# Architecture and Engineering Standards

This document establishes the mandatory architectural, design, and engineering rules for all backend services, APIs, and client-side integrations within the repository. Every engineer and AI agent must adhere strictly to these principles.

---

## 1. Architecture, Layering & Communication

### 1.1 Strict Layering & Separation of Concerns
- **Execution Flow**: Flow of control must strictly follow `Controller` -> `Service` -> `Repository`.
- **Prohibited Calls**: `Controller` -> `Repository` direct invocations are strictly forbidden. Repositories must never be injected into controllers.
- **Responsibilities**:
  - `Controller`: Request mapping, DTO validation (`@Valid`), HTTP status codes, and delegating to services.
  - `Service`: Business logic orchestration, validation, domain entity lifecycle, and transaction management (`@Transactional`).
  - `Repository`: Data persistence, query execution, and database dialect interactions.
- **Dependency Hygiene**: Circular dependencies between beans or packages are strictly prohibited. Enforce constructor-based injection via Lombok `@RequiredArgsConstructor` with `final` fields.

### 1.2 DTO & Record Encapsulation
- **Zero Entity Leakage**: JPA `@Entity` classes must never be returned directly in REST API responses, accepted as request bodies, or serialized into public message queues.
- **Immutable DTOs**: Always map entities to immutable Data Transfer Objects, utilizing Java 21 `record`s.
- **Prevent Schema Exposure & LazyInitializationException**: Direct entity exposure risks accidental database schema leakage, unintended cascading updates through JSON deserialization, Jackson circular serialization loops, and runtime `LazyInitializationException` outside active sessions.

### 1.3 Mandatory Network Timeouts
- **Explicit Timeout Enforcement**: Every outbound network client—including Spring `RestClient`, `WebClient`, `RestTemplate`, OpenFeign clients, gRPC stubs, and third-party SDKs—must declare explicit connection and read/socket timeouts.
- **Zero Defaults**: Defaulting to zero or infinite timeouts is strictly prohibited.
- **Recommended Thresholds**:
  - Connection Timeout (`connectTimeout`): <= 3000 ms.
  - Read / Socket Timeout (`readTimeout`): <= 5000 ms (adjust according to partner SLA, never exceeding 10s without approval).

### 1.4 Circuit Breakers & Graceful Degradation
- **Resilience Wrapping**: All synchronous inter-service and external 3rd-party HTTP calls must be protected with Resilience4j circuit breakers, bulkheads, and retry limiters.
- **Mandatory Fallback Logic**: Every circuit breaker must implement a graceful fallback method (e.g., returning cached data, default degraded responses, or queuing for asynchronous retry). Cascading failures that propagate external downtime to upstream users are unacceptable.

### 1.5 Client & Mobile Security Boundaries
- **Zero Secrets in Clients**: Never expose private API keys, service credentials, database connection strings, or payment gateway secret keys in client-side applications (Flutter mobile app bundles, web frontends, or public environment variables such as `NEXT_PUBLIC_` or `VITE_`).
- **Backend as Gateway**: The mobile and web frontends must interact exclusively with our authenticated, TLS-encrypted backend API. The backend orchestrates calls to 3rd-party providers securely.

---

## 2. Data Integrity & Concurrency Control

### 2.1 Transaction Management & Scope
- **Write Boundaries**: Every business operation involving multi-table writes, updates, or state transitions must be enclosed in `@Transactional` at the service layer.
- **Read-Only Optimization**: Read-heavy queries should be marked `@Transactional(readOnly = true)` to avoid Hibernate dirty-checking overhead and allow connection routing to read replicas.
- **Keep Transactions Lean**: Do not keep transactions open longer than necessary. Execute validations and preliminary non-DB operations prior to opening the transaction boundary.

### 2.2 Remote I/O Isolation from Transactions
- **Strict Network I/O Ban**: Network operations (HTTP calls, email/SMS dispatch, payment gateway requests, message publishing) must **NEVER** be executed inside an active `@Transactional` method.
- **Connection Pool Starvation**: Network latencies hold database connections open for seconds, rapidly exhausting the HikariCP pool and causing system-wide cascading failure.
- **Approved Pattern**:
  1. Complete database mutations inside a concise `@Transactional` method.
  2. Commit the transaction and release the database connection.
  3. Execute outbound network I/O or publish events (see Section 2.5).

### 2.3 Race Condition Mitigation & Locking Strategies
Critical concurrent workflows (e.g., wallet deduction, inventory reservation, seat booking, order status updates) must employ deterministic locking:
- **Pessimistic Locking**: Use `LockModeType.PESSIMISTIC_WRITE` (`SELECT ... FOR UPDATE`) when contention is high and operations must serialize deterministically.
- **Optimistic Locking**: Use `@Version` timestamp/number columns on entities for low-to-medium contention scenarios; always catch and handle `OptimisticLockingFailureException` with an appropriate retry policy.
- **Atomic Database Updates**: Favor direct atomic SQL updates where viable (e.g., `UPDATE wallet SET balance = balance - :amount WHERE id = :id AND balance >= :amount`).

### 2.4 Safe Distributed Locking (Redis / Redisson)
When cross-instance distributed locks are necessary:
1. **Mandatory TTL**: Locks must always have an explicit expiration time (lease time) to guarantee automatic release if the holding instance crashes.
2. **Unique Ownership Token**: Each lock acquisition must store a unique request/thread identifier (UUID) as the lock value.
3. **Atomic Lua Release**: Locks must only be released via an atomic Lua script verifying ownership:
   ```lua
   if redis.call('get', KEYS[1]) == ARGV[1] then
       return redis.call('del', KEYS[1])
   else
       return 0
   end
   ```
4. **Final DB Guardrail**: Distributed locks can expire under heavy GC pauses or network partitions. Therefore, the database unique constraint or atomic state check remains the ultimate source of truth.

### 2.5 Dual-Write Prevention & Event Publishing
- **Never Dual-Write Directly**: Never write to a database and directly publish to Kafka/RabbitMQ in the same method without consistency safeguards.
- **Transactional Outbox Pattern**: Persist domain events into an outbox database table within the same ACID transaction as the business entity. A reliable relay process (Debezium CDC or polling worker) publishes outbox records to the broker.
- **Spring Transaction Listener**: Alternatively, use `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to ensure events are dispatched only after the database transaction has committed successfully.

### 2.6 State Machine Safety & Timeout Recovery
- **Deterministic State Transitions**: Stateful entities (Orders, Bookings, Payments, Shipments) must enforce strict finite state machine (FSM) validation. Invalid transitions must fail fast with descriptive domain errors.
- **Automated Timeout Sweepers**: Implement scheduled sweepers to identify and resolve stuck intermediate states (e.g., auto-canceling orders in `PAYMENT_PENDING` after 15 minutes, expiring unassigned ride requests).
- **Emergency Cancellation & Compensation**: Provide idempotent cancellation and compensation endpoints (Saga pattern) to safely revert distributed side effects upon downstream failures.

---

## 3. Performance, Database & Caching

### 3.1 1-to-1 Schema & ORM Parity
- **Flyway / Liquibase as Source of Truth**: All database schema changes must be driven by versioned database migrations.
- **Strict Parity**: JPA entity table names, column names, column data types, string lengths, indexes, and nullability constraints must mirror SQL migration scripts exactly.
- **Production DDL Policy**: `spring.jpa.hibernate.ddl-auto` must be set to `validate` or `none` in all staging and production environments; `update` and `create-drop` are strictly prohibited.

### 3.2 Indexing Strategy & Hygiene
- **Foreign Key Indexing**: Every foreign key column (`@JoinColumn`) must have an explicit database index to avoid full table scans during JOINs and cascade operations.
- **Filter & Sort Optimization**: Columns frequently appearing in `WHERE` filters, `ORDER BY` clauses, or soft-delete flags (`status`, `tenant_id`, `created_at`, `deleted_at`) must be indexed.
- **Eliminate Redundancy**: Avoid creating redundant indexes on columns that already serve as the leading column in composite indexes or unique constraints.

### 3.3 Elimination of N+1 Queries & Enforced Pagination
- **Mandatory Pagination**: All list and search endpoints must accept `Pageable` parameters and enforce a strict upper bound on `pageSize` (default: 20, absolute maximum: 50 or 100). Unbounded `findAll()` queries returning entire tables into heap memory are strictly banned.
- **N+1 Prevention**:
  - Use `JOIN FETCH` queries or `@EntityGraph(attributePaths = {...})` to load related entities in a single query.
  - Set `spring.jpa.properties.hibernate.default_batch_fetch_size: 30` to batch lazy collections if join fetching is impractical.

### 3.4 Connection Pool Tuning (HikariCP & Redis)
- **HikariCP Sizing**: Calculate pool size based on hardware: `pool_size = (core_count * 2) + effective_spindle_count`.
- **Multi-Instance Quota**: Verify that total connections across all application instances do not exceed the database server's `max_connections` (e.g., `num_instances * hikari_max_pool_size < postgres_max_connections`).
- **Redis Connection Pool**: Configure Lettuce or Jedis connection pools with explicit max-active, min-idle, and timeout parameters to prevent connection leaks during traffic spikes.

### 3.5 Safe Caching Strategy
- **Read-Heavy Caching**: Cache expensive, infrequently changing query results using Redis or Caffeine.
- **Explicit Invalidation**: Mutating operations must explicitly invalidate affected caches using `@CacheEvict(allEntries = true)` or fine-grained key eviction.
- **Stampede Mitigation**: Guard against Cache Stampede on hot keys using single-flight loaders, background pre-warming, or randomized TTL jitter (`ttl = base_ttl + rand(0, 60s)`).
- **Penetration Mitigation**: Guard against Cache Penetration (queries for non-existent IDs) by caching null/empty values with a short TTL (e.g., 60 seconds) or deploying Bloom filters.

---

## 4. Security & Access Control

### 4.1 Token Lifecycle & Revocation
- **Short-Lived Access Tokens**: Access Tokens must have a short TTL (15 to 30 minutes). Refresh Tokens may have longer validity (7 to 30 days).
- **Explicit Claim Separation**: Tokens must contain an explicit type claim (`type: "ACCESS"` vs `type: "REFRESH"`). Refresh tokens must be rejected if presented to regular business endpoints.
- **Active Invalidation**: Provide immediate token blacklisting or session termination in Redis upon user logout, password reset, or permission revoking.

### 4.2 Role-Based Access Control (RBAC) & Method Security
- **Method-Level Security**: Apply `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("hasAuthority('SCOPE_...')")` on all sensitive controller methods.
- **Secure Public Registration**: Public sign-up endpoints must never accept client-supplied role parameters. Newly registered accounts must strictly default to the lowest privilege role (e.g., `ROLE_USER`).

### 4.3 BOLA / IDOR Defense
- **Ownership Verification**: Direct access to resources via identifiers (`GET /api/v1/orders/{id}`, `DELETE /api/v1/files/{id}`) must strictly verify that the requesting user owns the resource or has authorized administrative scope.
- **Never Trust Client Context**: Retrieve `currentUserId` and `tenantId` exclusively from the authenticated security context (`SecurityContextHolder`), never from client query parameters or request bodies.

### 4.4 Input Validation & Gateway Defense
- **Rigorous DTO Validation**: Annotate all incoming request payloads with `@Valid` and apply Jakarta validation constraints (`@NotNull`, `@NotBlank`, `@Size`, `@Pattern`, `@Positive`).
- **SQLi & XSS Prevention**: Use parameterized queries exclusively. Sanitize user-provided text inputs and reject unescaped HTML tags.
- **Strip Internal Headers at Gateway**: The API Gateway / Ingress controller must strip all internal identity headers (such as `X-User-Id`, `X-User-Role`, `X-Tenant-Id`) from incoming client requests to prevent header spoofing.

### 4.5 CORS, Security Headers & Rate Limiting
- **Strict CORS**: Whitelist only trusted, known client origins. Wildcard origins (`*`) combined with `allowCredentials=true` are strictly forbidden.
- **Mandatory Headers**: Enforce `Strict-Transport-Security` (HSTS), `Content-Security-Policy` (CSP), `X-Frame-Options: DENY`, and `X-Content-Type-Options: nosniff`.
- **Endpoint Rate Limiting**: Apply token-bucket or sliding-window rate limiting (via Redis or Bucket4j) on authentication, OTP verification, password reset, and payment endpoints.

### 4.6 Webhook Security & PII Protection
- **HMAC Signature Verification**: All inbound webhooks (payment providers, third-party integrations) must verify HMAC-SHA256 signatures before processing payloads.
- **Zero Secrets in Code**: API keys, signing secrets, and certificates must be injected via environment variables or secret vaults.
- **PII Redaction**: Passwords, payment card numbers, social security identifiers, and personal data must be excluded from logs, error messages, and public API responses.

---

## 5. Fault Tolerance & Asynchronous Processing

### 5.1 Idempotency for Mutating Operations
- **Idempotency Keys**: Critical mutating endpoints (charge, transfer, place order) must accept a unique `Idempotency-Key` header.
- **Atomic Deduplication**: Verify and store idempotency keys in Redis or database with a TTL. Subsequent requests with the same key must return the cached initial response without re-executing business logic.
- **Consumer Idempotency**: Queue consumers must maintain an idempotency check table or Redis set to safely handle at-least-once message delivery.

### 5.2 Consumer Resilience, Backoff & Dead Letter Queues (DLQ)
- **Zero Exception Swallowing**: Asynchronous workers and message listeners (Kafka, RabbitMQ, SQS) must never silently catch and ignore unhandled exceptions.
- **Exponential Backoff**: Configure retry templates with exponential backoff and jitter for transient errors (network hiccups, DB timeouts).
- **Dead Letter Topic (DLT/DLQ)**: Non-retryable errors (deserialization failures, validation errors) and messages exceeding maximum retry thresholds must route to a Dead Letter Queue with operational alerting.
- **Concurrency & Prefetch**: Set bounded prefetch counts and consumer concurrency to prevent workers from exhausting JVM memory.

---

## 6. Operations, Background Tasks & Observability

### 6.1 Distributed Scheduled Jobs
- **Multi-Instance Coordination**: Background cron jobs (`@Scheduled`) running in a multi-instance cluster must utilize distributed locking (e.g., ShedLock with Redis/JDBC) to ensure single-instance execution.
- **Explicit Lock Durations**: Define `lockAtMostFor` (to release locks if an instance crashes) and `lockAtLeastFor` (to prevent re-execution when clocks slightly differ across instances).

### 6.2 Graceful Shutdown & Health Probes
- **Graceful Termination**: Enable `server.shutdown=graceful` and configure `spring.lifecycle.timeout-per-shutdown-phase: 30s` to allow in-flight requests to complete before process exit.
- **Actuator Health Checks**: Expose Spring Boot Actuator `/actuator/health/liveness` and `/actuator/health/readiness` probes. The readiness probe must verify live connectivity to PostgreSQL, Redis, and message brokers.

### 6.3 Centralized Error Handling, Logging & Tracing
- **RFC 7807 ProblemDetail**: Implement a centralized `@RestControllerAdvice` that maps uncaught exceptions into standard RFC 7807 `ProblemDetail` structures.
- **Never Leak Stack Traces**: Suppress internal exception names and raw stack traces from client responses in production.
- **MDC Correlation Tracing**: Extract or generate a Correlation ID (`X-Trace-Id`) in a servlet filter, inject it into SLF4J MDC, propagate it across asynchronous threads and outbound HTTP calls, and include it in response headers.
- **Log Hygiene**: Sanitize all log statements. Never log plain-text passwords, authorization headers, or customer PII.

---

## 7. Conditional & System-Specific Rules

### 7.1 Multi-Tenant Data Isolation
- **Tenant Context Propagation**: When operating in a multi-tenant environment, the tenant identifier must be resolved at the security/gateway layer and stored in a scoped `TenantContext`.
- **Mandatory Query Filtering**: Every database query, update, and delete operation must include `tenant_id` filtering (via Hibernate `@TenantId`, custom entity listeners, or schema-per-tenant routing) to prevent cross-tenant data leaks.

### 7.2 File Upload & Streaming Architecture
- **Size Limits**: Enforce strict file size limits (`spring.servlet.multipart.max-file-size` and `max-request-size`).
- **Stream, Never Buffer**: Never read large files entirely into memory as `byte[]`. Stream files directly to cloud storage (S3/MinIO) or issue pre-signed upload URLs so clients upload directly to object storage.
- **MIME & Extension Validation**: Validate file extensions and verify actual magic bytes (MIME type) before accepting uploads.

### 7.3 Real-Time Communication (WebSocket & SSE)
- **Lifecycle Management**: Cleanly track open WebSocket sessions and SSE emitters; deregister connections on disconnect, timeout, or error.
- **Heartbeat / Ping-Pong**: Implement periodic heartbeats to detect orphaned connections and free network sockets.
- **Multi-Instance Broadcast**: In multi-instance deployments, use a Redis Pub/Sub backplane or message broker relay (STOMP) to route real-time messages across cluster nodes.

### 7.4 Serverless & Container Cold Start Optimization
- **Startup Optimization**: For serverless functions or aggressively scaled containers, minimize reflection, enable Spring AOT / GraalVM native images where appropriate, and tune JVM container memory limits (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
- **Connection Multiplexing**: Use database connection proxies (e.g., PgBouncer or AWS RDS Proxy) to prevent serverless burst functions from overloading database connection limits.
