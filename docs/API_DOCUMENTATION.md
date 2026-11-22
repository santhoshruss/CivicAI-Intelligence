# CivicAI REST API Documentation

Base URL: `http://localhost:8080/api`

All endpoints return a standardized JSON structure:
```json
{
  "success": true,
  "message": "Operation description",
  "data": { ... },
  "timestamp": "2026-11-21T19:00:00"
}
```

---

## 1. Authentication APIs (`/api/auth`)

### `POST /api/auth/register`
Registers a new Citizen account.
- **Request Body**:
  ```json
  {
    "name": "Jane Doe",
    "email": "jane@example.org",
    "password": "Password@123"
  }
  ```
- **Response**: JWT access token, refresh token, and user details.

### `POST /api/auth/login`
Authenticates user and returns JWT token.
- **Request Body**:
  ```json
  {
    "email": "officer@civicai.gov",
    "password": "Password@123"
  }
  ```
- **Response**: Bearer access token, user role, and department.

### `POST /api/auth/refresh`
Rotates and issues a new access token using a valid refresh token.

### `POST /api/auth/logout`
Revokes active refresh token.

### `GET /api/auth/me`
Retrieves currently authenticated user profile.

---

## 2. Incident APIs (`/api/incidents`)

### `POST /api/incidents`
Submit a new infrastructure damage report (Citizen or Admin).
- **Request Body**:
  ```json
  {
    "category": "POTHOLE",
    "description": "Large cavity on westbound traffic lane.",
    "latitude": 37.774929,
    "longitude": -122.419416,
    "address": "Market St & 7th St"
  }
  ```

### `GET /api/incidents`
List incidents with filtering (`category`, `status`, `priority`, `search`) and pagination (`page`, `size`, `sortBy`).

### `GET /api/incidents/my`
List reports submitted by currently authenticated Citizen.

### `GET /api/incidents/{id}`
Retrieve complete incident dossier.

### `PATCH /api/incidents/{id}/status`
Update incident status and approved priority (Municipal Officer / Admin).
- **Request Body**:
  ```json
  {
    "status": "IN_PROGRESS",
    "priority": "HIGH",
    "explanation": "Severity elevated due to proximity to school crossing."
  }
  ```

### `POST /api/incidents/{id}/evidence`
Upload evidence photo (Multipart form-data: `file`).

### `POST /api/incidents/{id}/analyze`
Trigger AI computer vision damage analysis.

### `GET /api/incidents/{id}/analysis`
Get latest model classification result, confidence, and explanation.

### `GET /api/incidents/{id}/duplicates`
Scan and return potential duplicate incidents within 300 meters.

### `POST /api/incidents/{id}/duplicates/confirm`
Review and set duplicate status (`CONFIRMED_DUPLICATE` or `DISMISSED`).

---

## 3. Work Order APIs (`/api/work-orders`)

### `POST /api/work-orders`
Create a work order draft (Officer / Admin).

### `GET /api/work-orders`
List work orders. Filterable by `status`, `priority`, `departmentId`.

### `POST /api/work-orders/{id}/approve`
Officially authorize a draft work order (Officer / Admin).

### `POST /api/work-orders/{id}/assign`
Assign an approved work order to a Field Inspector.
- **Request Body**: `{"inspectorId": 3}`

### `PATCH /api/work-orders/{id}/status`
Update work order progress (`IN_PROGRESS`, `RESOLVED`, etc.) with inspection notes.

---

## 4. RAG Knowledge APIs (`/api/knowledge`)

### `POST /api/knowledge/upload`
Upload and index maintenance manual PDF into FAISS vector database.

### `POST /api/knowledge/query`
Query guidelines using semantic vector retrieval.
- **Request Body**: `{"query": "What is the asphalt tack coat density for road repair?"}`
- **Response**: Grounded answer with exact document name, page citations, and excerpt.

### `GET /api/knowledge/documents`
List all indexed maintenance manuals and chunk statistics.

---

## 5. Agentic AI APIs (`/api/agent`)

### `POST /api/agent/analyze/{incidentId}`
Executes the 8-tool autonomous triage coordinator. Returns recommendation briefing and tool audit logs.

### `GET /api/agent/runs/{id}`
Retrieve historical agent execution record and step-by-step tool logs.

---

## 6. Dashboard & Admin APIs (`/api/dashboard`, `/api/admin`)

- `GET /api/dashboard/stats`: KPI totals and category/priority breakdowns.
- `GET /api/dashboard/recent-incidents`: Top 10 recent submissions.
- `GET /api/admin/users`: User directory and role assignments.
- `PATCH /api/admin/users/{id}/role`: Modify user role, department, or active status.
- `GET /api/admin/audit-logs`: Paginated immutable system activity trail.


---

## 8. Hugging Face AI Endpoints (`http://localhost:8000/api`)

### `POST /api/ai/detect-duplicates`
Compares a target incident with nearby candidate incidents using Sentence Transformers (`all-MiniLM-L6-v2`) semantic embeddings, Haversine GPS proximity (300m), and category weighting.
- **Request Body**:
  ```json
  {
    "target": {
      "id": 1,
      "latitude": 13.0827,
      "longitude": 80.2707,
      "category": "POTHOLE",
      "description": "Deep asphalt crater on Main Road"
    },
    "candidates": [
      {
        "id": 2,
        "reference_number": "INC-2026-0002",
        "category": "POTHOLE",
        "description": "Large road cavity punctured motorcycle tire",
        "latitude": 13.0830,
        "longitude": 80.2709
      }
    ],
    "radius_meters": 300.0
  }
  ```
- **Response**:
  ```json
  {
    "matches": [
      {
        "related_incident_id": 2,
        "reference_number": "INC-2026-0002",
        "distance_meters": 39.77,
        "semantic_similarity": 0.68,
        "similarity_score": 0.79,
        "similarity_percentage": 79,
        "classification": "HIGH_SIMILARITY",
        "explanation": "68% semantic textual alignment. Immediate proximity (39m away). Matching category 'POTHOLE'."
      }
    ]
  }
  ```

### `POST /api/ai/process-complaint`
Performs language detection (English, Tamil, Tanglish), transliteration/normalization to English, and category/urgency intent extraction.
- **Request Body**:
  ```json
  {
    "text": "Enga street la romba periya pothole irukku, accident aagudhu."
  }
  ```
- **Response**:
  ```json
  {
    "detected_language": "Tanglish",
    "original_text": "Enga street la romba periya pothole irukku, accident aagudhu.",
    "normalized_english": "our on the street very large pothole is present, causing accidents.",
    "predicted_category": "POTHOLE",
    "urgency": "HIGH",
    "suggested_department": "Road Maintenance",
    "confidence": 0.88
  }
  ```

### `POST /api/ai/transcribe-audio`
Transcribes audio recordings into text using Hugging Face Whisper (`openai/whisper-tiny` / `whisper-small`).
- **Multipart Form**: `file` (WAV/WebM audio)
- **Response**:
  ```json
  {
    "transcription": "Severe pothole near Anna Nagar bus stand",
    "status": "SUCCESS",
    "model_name": "HuggingFace/openai/whisper-tiny",
    "duration_seconds": 3.4
  }
  ```

### `POST /api/ai/ocr-document`
Extracts text and metadata from scanned municipal maintenance SOPs or inspection photos using PyMuPDF and RapidOCR ONNX.
- **Multipart Form**: `file` (PDF/Image)
- **Response**:
  ```json
  {
    "filename": "inspection_report.pdf",
    "total_pages": 4,
    "ocr_applied": true,
    "metadata": {
      "title": "Road Maintenance Inspection Report",
      "department": "Road Infrastructure Department",
      "document_date": "November 2026 Revision"
    }
  }
  ```

### `POST /api/ai/predict-priority`
Predicts incident priority using a trained Scikit-Learn Random Forest Classifier (`CivicAI-PriorityClassifier-RF-v1.0`, Accuracy 97.5%).
- **Request Body**:
  ```json
  {
    "category": "STREETLIGHT_DAMAGE",
    "description": "Electric wire snapped and sparking on public street",
    "confidence": 0.94,
    "duplicate_count": 1
  }
  ```
- **Response**:
  ```json
  {
    "predicted_priority": "CRITICAL",
    "probabilities": {"CRITICAL": 0.88, "HIGH": 0.10, "MEDIUM": 0.02, "LOW": 0.00},
    "model_version": "CivicAI-PriorityClassifier-RF-v1.0",
    "is_ml": true,
    "explanation": "Critical hazard/safety terms detected. 1 nearby duplicate report(s) amplify public urgency."
  }
  ```
