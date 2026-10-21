# Project Guidelines: myfschoolse1945

## Project Overview
This repository contains a full-stack multi-tier application:
- `backend/`: Spring Boot 3.x with Java 21, Spring Data JPA, Spring Security, REST API.
- `mobile/`: Flutter (Dart 3.x), MVVM/MVC, State management, Mobile Client.

---

## Architecture & Engineering Standards (Mandatory Compliance)

All agents and developers working on this repository **MUST** strictly adhere to the project architecture, design, and engineering rules defined in:
👉 [`.agents/rules/architecture-and-engineering.md`](.agents/rules/architecture-and-engineering.md)

### Key Architectural Pillars:
1. **Architecture, Layering & Communication**: Strict Controller -> Service -> Repository hierarchy, immutable DTO/Record encapsulation (zero JPA `@Entity` exposure), mandatory network timeouts on all outbound clients, Resilience4j circuit breakers with fallback, zero secrets in mobile/frontend clients.
2. **Data Integrity & Concurrency Control**: `@Transactional` boundaries, zero remote I/O inside database transactions, race condition mitigation (pessimistic/optimistic locking, atomic SQL updates), safe Redis distributed locking with TTL and atomic Lua release, dual-write prevention via Transactional Outbox or `@TransactionalEventListener`, state machine safety with timeout recovery.
3. **Performance, Database & Caching**: 1-to-1 schema & ORM parity with database migrations, comprehensive indexing on foreign keys and filter columns, zero N+1 queries (`JOIN FETCH` / `@EntityGraph` / batch fetching), mandatory pagination with max page size caps, HikariCP/Redis connection pool tuning, safe caching with stampede and penetration defense.
4. **Security & Access Control**: Short-lived access tokens, explicit token claim separation, token revocation/blacklisting in Redis, method security (`@PreAuthorize`), strict BOLA/IDOR ownership checks, input validation (`@Valid`), gateway stripping of internal identity headers, strict CORS, security headers, rate limiting, and webhook HMAC-SHA256 signature verification.
5. **Fault Tolerance & Asynchronous Processing**: Idempotency keys for mutating endpoints and consumer deduplication, resilient queue listeners with exponential backoff and Dead Letter Queue (DLQ/DLT) routing, bounded consumer concurrency.
6. **Operations, Background Tasks & Observability**: Distributed scheduled jobs via ShedLock/Redis locks, graceful shutdown (`server.shutdown=graceful`), Actuator liveness/readiness health probes, centralized RFC 7807 `ProblemDetail` error handling, SLF4J MDC trace ID propagation, and log sanitization.
7. **Conditional & System-Specific Rules**: Multi-tenant query isolation by `tenant_id`, streaming file uploads with size caps, real-time WebSocket/SSE connection lifecycle management with Redis Pub/Sub sync, and container/serverless cold start optimization.

---

## SonarQube MCP Protocol (Code Quality & Security Gate)

When interacting with the `sonarqube` MCP server, the Agent **MUST** strictly adhere to the following protocol:

### 7.1. Trigger Timing (When to Invoke) & Mandatory Post-Task Gate
- **Mandatory Post-Task Gate:** At the conclusion of ANY task, feature implementation, or bugfix (prior to producing `walkthrough.md` or claiming completion), the Agent **MUST ALWAYS**:
  1. Compile and pass all local test suites (`./gradlew test`, `flutter test`).
  2. Execute the Sonar scan script: `powershell -File C:\Users\vanan\myfschoolse1945\run_scanner_with_cred.ps1`.
  3. Query SonarQube MCP (`search_sonar_issues_in_projects`) with `inNewCodePeriod: false` to inspect all modified files.
  4. Resolve 100% of reported issues before closing the turn.
- **Strictly Prohibited:** **NEVER** invoke the SonarQube MCP during incremental edits, after trivial single-line changes, or when modifying documentation (`.md`) or static configurations (`.json`, `.yaml`, `.xml`, `.properties`).

### 7.2. Tool Selection & Freshness Protocol (Full-File & Test Scope Parity)
- **Local Validation First:** Always verify changes locally with native project toolchains (`./gradlew compileJava test`, `flutter analyze`, `flutter test`) before involving Sonar.
- **Cloud Query Prerequisites & Full-File Scope:** SonarQube Cloud (`anvvhe190784`) reflects **ONLY** the most recently uploaded scan. **NEVER** re-query cloud issue endpoints (`search_sonar_issues_in_projects`, `get_component_measures`, `get_project_quality_gate_status`) immediately after local file edits without first running a fresh local scan (`powershell -File C:\Users\vanan\myfschoolse1945\run_scanner_with_cred.ps1`) and waiting for successful completion.
- **Full-File & Test Scope Parity:** To match SonarLint / SonarQube for IDE 100%, queries to `search_sonar_issues_in_projects` MUST explicitly specify `inNewCodePeriod: false` across touched components. Both main and test sources must be verified.
- **Local Snippet Checks:** Use `analyze_code_snippet` in `ide`/`analysis` toolsets for quick single-snippet checks when full scans are uneconomical.

### 7.3. Severity & Triage Hierarchy (Zero-Tolerance / 100% Resolution Policy)
- **Zero-Ignored Issues Mandate (100% Resolution):** The Agent **MUST NOT** skip or ignore any finding reported by SonarQube, regardless of category (`Bug`, `Vulnerability`, `Security Hotspot`, `Code Smell`) or severity level (`Blocker`, `Critical`, `Major`, `Minor`, `Info`). Every single finding must be explicitly resolved before task completion.
- **Ordered Remediation Pipeline:**
  1. **Primary Security & Stability (Blocker / Critical / Major):** Resolve bugs, vulnerabilities, and high-risk hotspots first to guarantee runtime correctness and security.
  2. **Code Cleanliness & Maintainability (Code Smell / Minor / Info):** Resolve all cognitive complexity, dead code, duplicate literals, missing `const`, and style/timezone suggestions to eliminate technical debt completely. Always call `show_rule` first when unfamiliar with canonical remediation patterns.
- **Explicit Suppression Standard:** If a finding is a genuine false positive or conflicts with framework design (e.g. `@SuppressWarnings("java:S4502")` for stateless JWT CSRF or `// ignore: <diagnostic>` in Dart), silent suppression is **STRICTLY PROHIBITED**. The Agent **ALWAYS** must include an inline explanatory comment justifying the architectural rationale.

### 7.4. Anti-Loop & Convergence Guardrail
- **Maximum 2 Iterations:** Limit remediation attempts for any specific Sonar finding to a maximum of **2 iterations**.
- **Hard Stop Condition:** If an issue remains unresolved after 2 attempts, or if the recommended remediation risks breaking business logic or causes test regressions, **STOP IMMEDIATELY**, revert to the last working state, and present a concise technical summary to the user for an architectural decision.

### 7.5. Secret Protection & Token Hygiene
- **Zero Token Leakage:** The SonarCloud token (`SONARQUBE_TOKEN`) **MUST NEVER** be printed in chat, logged to console, written into committed source files, or exposed in shell outputs. Always inject credentials via environment variables or Windows Credential Manager.

### 7.6. Zero-Baseline Sonar Scaffolding & Dynamic Source Discovery Protocol
- **Cold-Start Manifest Detection:** When onboarding or working with a repository where `sonar-project.properties` is missing or unconfigured, the Agent **MUST** inspect project manifests (`pom.xml`, `build.gradle*`, `package.json`, `pubspec.yaml`, `Cargo.toml`, etc.) during Phase 1 Discovery to determine active modules and source structures.
- **Module Root Scoping (Solution 1 Standard):** Never configure overly restrictive language-specific subdirectories (such as `src/main/java`) in `sonar.sources`. Always configure the module source root:
  - **Java / Spring Boot:** `sonar.sources=<module>/src/main`, `sonar.tests=<module>/src/test` (guarantees automatic analysis of static assets, templates, SQL, JS, CSS, and HTML alongside Java/Kotlin).
  - **Flutter / Dart:** `sonar.sources=<module>/lib`, `sonar.tests=<module>/test`.
  - **Node.js / Frontend (React, Vue, Vite):** `sonar.sources=<module>/src` (or root `src`).
- **Standard Baseline Exclusions:** Every generated or updated `sonar-project.properties` MUST declare comprehensive exclusions to prevent build artifacts, package dependencies, and generated boilerplate from polluting analysis:
  `sonar.exclusions=**/build/**,**/.gradle/**,**/.dart_tool/**,**/node_modules/**,**/*.g.dart,**/*.freezed.dart,**/dist/**,**/out/**`
- **Existing Config Audit:** If `sonar-project.properties` already exists with narrow paths (e.g., ending in `/java`), the Agent must proactively upgrade them to module roots (`/src/main`) to maintain full multi-language coverage across the repository.

### 7.7. Connected Mode & Pre-Scan SCM Blame Guardrail
- **Connected Mode Standard:** Every repository analyzed by SonarQube/SonarCloud must maintain `.sonarlint/connectedMode.json` at the root directory containing `sonarCloudOrganization` and `projectKey` to ensure SonarQube for IDE (SonarLint) synchronizes 100% with the cloud project's Quality Profile and issue lifecycle.
- **Pre-Scan SCM Intent Tracking (`git add -N`):** Prior to executing the Sonar scanner script, the Agent **MUST** register all newly created files/directories into Git index via `git add -N .` (or specific paths). This prevents SonarScanner from raising `Missing blame information` warnings and ensures accurate line-level issue attribution on SonarCloud.


