# CivicAI Security Architecture and Policy

## Security Overview

CivicAI implements defense-in-depth across the presentation, business logic, persistence, and artificial intelligence layers. The system is designed to comply with modern public infrastructure standards, protecting citizen privacy, preventing unauthorized access, and securing automated intelligence workflows.

---

## 1. Authentication & Session Management
- **BCrypt Password Hashing**: Passwords are never stored in plaintext. They are salted and hashed using BCrypt (work factor 12).
- **Stateless JWT Tokens**: Authentication uses HMAC-SHA256 signed JSON Web Tokens containing user ID, role, and expiration timestamp.
- **Refresh Token Rotation**: Refresh tokens are stored in the database with one-time use rotation and revocation capabilities.
- **No Sensitive Data in Payloads**: Tokens contain only non-sensitive subject identifiers and granted authorities.

---

## 2. Server-Side Role-Based Access Control (RBAC)
Role enforcement occurs strictly in the Spring Boot backend via Spring Security filters and Method Security (`@PreAuthorize`), never trusting frontend state:
- `CITIZEN`: Can report incidents, view only their own submitted incidents, and track resolution status.
- `MUNICIPAL_OFFICER`: Can inspect all incidents, trigger AI triage, review and override priorities with logged explanations, approve work orders, and assign inspectors.
- `FIELD_INSPECTOR`: Can view assigned work orders, update repair progress, and upload post-inspection verification evidence.
- `ADMIN`: Has full user management, role delegation, department creation, and system audit log visibility.

---

## 3. Secure File Uploads & Path Traversal Defense
- **Stored Filename Randomization**: All uploaded evidence files (photos, repair receipts) are saved with generated UUIDs (e.g., `d4f8e..._pothole.jpg`), preventing arbitrary overwrite or direct name execution.
- **Path Traversal Protection**: Upload handlers validate normalized paths using `Paths.get().normalize()` and ensure files remain strictly within the designated uploads folder.
- **Content Type & Size Whitelisting**: Strictly restricts files to `image/jpeg`, `image/png`, `image/webp`, and `application/pdf`. Enforces max upload limits (10MB for photos, 25MB for SOP documents).
- **Out-of-Web-Root Storage**: Evidence is stored outside the public HTTP web root and served exclusively through authenticated Spring Boot controller endpoints with ownership checks.

---

## 4. Frontend Injection & Cross-Site Scripting (XSS)
- **Safe DOM APIs**: All frontend views use `textContent`, `setAttribute`, and DOM node construction. `innerHTML` is strictly guarded or avoided for unescaped user inputs.
- **Content Security**: Sanitization of address coordinates, descriptions, and user notes before rendering in maps or tables.

---

## 5. RAG & Agentic AI Security Controls
- **Prompt Injection Defense**: Ingested PDFs are treated as untrusted data. When querying maintenance manuals, vector chunks are quoted as reference context and cannot override agent system instructions.
- **Human-in-the-Loop Principle**: The Agentic AI coordinator generates draft assessments and recommendations, but **never automatically approves work orders, dispatches city crews, or mutates budget records** without authorized Municipal Officer sign-off.
- **Tool Whitelisting**: Agent execution is restricted to an explicit allowlist of 8 read-only or draft-only tools, preventing arbitrary command injection or unauthorized SQL execution.

---

## 6. Audit Logging & Traceability
- **Immutable Audit Trail**: All state-changing actions (user role updates, work order approvals, priority overrides, manual status changes) record user ID, action type, affected entity, timestamp, and client IP in `audit_logs`.
