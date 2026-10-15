# Project Guidelines: myfschoolse1945

## Project Overview
This repository contains a full-stack multi-tier application:
- `backend/`: Spring Boot 3.x with Java 21, Spring Data JPA, Spring Security, REST API.
- `mobile/`: Flutter (Dart 3.x), MVVM/MVC, State management, Mobile Client.

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
