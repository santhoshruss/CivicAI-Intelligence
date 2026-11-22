# CivicAI System Architecture & Data Flow

## 1. High-Level Architectural Diagram

```
+-----------------------------------------------------------------------------------+
|                           PRESENTATION LAYER (Browser)                            |
|  - HTML5 / CSS3 / Vanilla JavaScript (No heavy frameworks, fast zero-lag DOM)     |
|  - Leaflet.js + OpenStreetMap (Live Geolocation Map)                              |
|  - Chart.js (Real-time category & priority analytics)                             |
|  - Role-specific UI views (Citizen, Officer, Field Inspector, Admin)             |
+------------------------------------------+----------------------------------------+
                                           | HTTP REST / JSON (JWT in Header)
                                           v
+-----------------------------------------------------------------------------------+
|                        APPLICATION LAYER (Java Spring Boot 3)                     |
|  - Spring Security: Stateless JWT filter, BCrypt password hasher, RBAC filters    |
|  - Controller Layer: REST endpoints for auth, incidents, work orders, RAG, agent |
|  - Service Layer: Business logic, priority engine, duplicate logic, audit events |
|  - Persistence: Spring Data JPA + Hibernate                                       |
|  - File Storage: Randomized UUIDs, path normalization, safe MIME whitelisting    |
+---------------------+-------------------------------------+-----------------------+
                      |                                     |
    SQL via JDBC (8.0)|                                     | HTTP REST (JSON/Multipart)
                      v                                     v
+-----------------------------+     +-----------------------------------------------+
|      PERSISTENCE LAYER      |     |             AI MICROSERVICE LAYER             |
|          (MySQL 8)          |     |              (Python + FastAPI)               |
|                             |     |                                               |
|  - 14 Normalized Tables:    |     |  - Computer Vision Analysis:                  |
|    * users                  |     |    Extracts morphological depression, cavity, |
|    * departments            |     |    and edge texture features                  |
|    * incidents              |     |                                               |
|    * evidence               |     |  - Duplicate Incident Engine:                 |
|    * incident_analysis      |     |    Haversine geospatial distance & Jaccard   |
|    * duplicate_incidents    |     |                                               |
|    * maintenance_documents  |     |  - RAG Maintenance Knowledge Engine:         |
|    * work_orders            |     |    PyMuPDF (fitz) text extraction + chunking, |
|    * work_order_updates     |     |    Sentence Transformers embeddings, FAISS    |
|    * agent_runs             |     |    vector store, grounded source citations    |
|    * agent_tool_logs        |     |                                               |
|    * notifications          |     |  - Agentic AI Incident Coordinator:          |
|    * audit_logs             |     |    8-tool restricted workflow with structured |
|    * refresh_tokens         |     |    inputs/outputs & Human-in-the-Loop staging |
+-----------------------------+     +-----------------------------------------------+
```

---

## 2. Component Communication & Data Flow

### Workflow A: Incident Submission & Storage
1. **Citizen** submits damage report form with category, description, GPS coordinates, and photo evidence.
2. **Spring Boot** receives multipart request, validates inputs, and writes photo to disk with a cryptographically random UUID (`uploads/evidence/<uuid>.jpg`).
3. An `Incident` record is persisted in MySQL with status `REPORTED` and a unique reference number (`CIV-YYYY-XXXX`).
4. An in-app `Notification` is queued for the reporting citizen.

### Workflow B: AI Vision & Triage
1. **Municipal Officer** views incoming incidents on dashboard and triggers AI Analysis.
2. **Spring Boot** contacts the **FastAPI AI Service** via `POST /api/ai/analyze-image`.
3. AI Service calculates surface cavity depth, texture variance, and context keywords.
4. AI Service returns confidence score and classification status, saved in `incident_analysis`.

### Workflow C: Geospatial Duplicate Detection
1. **Duplicate Engine** calculates distance using the Haversine formula across active incidents within a 300-meter radius.
2. Matches are recorded in `duplicate_incidents` as `POSSIBLE_DUPLICATE`.
3. The Municipal Officer reviews and confirms or dismisses the duplicate with one click.

### Workflow D: RAG Maintenance Manual Retrieval
1. Administrator or Officer uploads municipal SOP manuals (PDF).
2. FastAPI extracts text using PyMuPDF, chunks the text with 100-character overlap, generates semantic embeddings via Sentence Transformers, and indexes them in a local FAISS vector store.
3. When querying maintenance protocols, FAISS retrieves top-$k$ nearest semantic neighbors and provides verifiable document name and page number citations.

### Workflow E: Agentic AI Workflow (8 Tools)
1. The **Agent Manager** coordinates an allowlisted 8-step pipeline:
   - `get_incident_details` -> `analyze_damage` -> `find_nearby_duplicates` -> `retrieve_maintenance_guidelines` -> `calculate_priority` -> `generate_recommendation` -> `create_draft_work_order` -> `generate_incident_summary`.
2. Every tool input and output is logged in `agent_tool_logs`.
3. A draft work order is staged in `DRAFT` status.
4. **Human-in-the-Loop Safeguard**: The agent cannot approve work orders or dispatch crews on its own; a Municipal Officer must review the briefing and sign off.
