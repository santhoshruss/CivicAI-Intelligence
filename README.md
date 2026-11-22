# CivicAI – AI Public Infrastructure Damage Intelligence System
> **Detect. Understand. Prioritize. Resolve.**

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Java: 17+](https://img.shields.io/badge/Java-17%2B-orange.svg)](https://openjdk.org/)
[![Spring Boot: 3.3.5](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Python: 3.11+](https://img.shields.io/badge/Python-3.11%2B-blue.svg)](https://www.python.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.110%2B-teal.svg)](https://fastapi.tiangolo.com/)
[![FAISS](https://img.shields.io/badge/Vector%20Store-FAISS-red.svg)](https://github.com/facebookresearch/faiss)
[![MySQL: 8.0+](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)

---

## 🌟 Project Overview
**CivicAI** is an autonomous municipal infrastructure intelligence web application that modernizes urban damage management. Citizens capture photo evidence and GPS coordinates of road cavities, electrical hazards, or water ruptures. CivicAI's computer vision evaluates the damage, the geospatial duplicate engine clusters redundant reports, a FAISS-powered RAG assistant consults official city SOPs, and an 8-tool Agentic AI workflow stages prioritized draft work orders for municipal officer authorization.

### 💰 Zero-Budget & 100% Open Source
- **Zero Paid Subscriptions**: Operates completely with open-source tools: Java 17, Spring Boot 3, MySQL 8, Python FastAPI, Hugging Face Transformers, Sentence Transformers, FAISS, PyMuPDF, RapidOCR, Leaflet, and Chart.js.
- **Hugging Face AI Integrated**:
  - **Semantic Duplicate Detection**: `sentence-transformers/all-MiniLM-L6-v2` dense cosine similarity with 300m Haversine proximity & category weighting.
  - **Computer Vision Damage Localization**: Hugging Face Vision (`google/mobilenet_v2_1.0_224`) with bounding box damage region overlays & labeled heuristic fallback.
  - **Multilingual NLP**: Language detection for English, Tamil script, and Tanglish (e.g., *"Enga street la romba periya pothole irukku"*), transliteration dictionary, and category intent classification.
  - **Whisper Voice Input**: Hugging Face Whisper (`openai/whisper-tiny`) audio transcription for hands-free complaint reporting.
  - **Document OCR**: PyMuPDF + RapidOCR ONNX for scanned inspection sheets and maintenance manual ingestion.
  - **Machine Learning Priority Prediction**: Scikit-Learn Random Forest Classifier (`CivicAI-PriorityClassifier-RF-v1.0`) with 97.5% Accuracy and explainability.
  - **8-Tool Agentic AI Workflow**: Wired to real MySQL records, real evidence images, real candidate duplicates, ML priority, and FAISS RAG citations.
- **Zero Paid API Keys Required**: All models run locally on CPU (Intel Core i5 compatible). Fallbacks are available for all services.

---

## 🏗️ System Architecture

```
[ Citizen / Officer Browser (HTML5, CSS3, Vanilla JS, Leaflet, Chart.js) ]
                               │
               REST / JSON (JWT Authorization Header)
                               ▼
            [ Spring Boot 3 Java Backend (:8080) ]
        ┌──────────────────────┼──────────────────────┐
        │                      │                      │
   Spring Data JPA       File Storage           HTTP REST
        │                      │                      │
        ▼                      ▼                      ▼
  [ MySQL 8.0 ]      [ Local Uploads ]     [ FastAPI AI Service (:8000) ]
(14 Normalized Tables) (UUID Protected)    ├── Computer Vision Triage
                                           ├── Geospatial Duplicate Engine
                                           ├── FAISS Vector Store (RAG)
                                           └── 8-Tool Agentic Coordinator
```

---

## 📁 Project Directory Structure

```
civicai/
├── frontend/                     # Modern CivicTech UI (Vanilla HTML, CSS, JS)
│   ├── index.html                # Landing Page (Hero, Features, How It Works)
│   ├── login.html                # Multi-Role Sign In with 1-Click Demo buttons
│   ├── register.html             # Citizen Registration
│   ├── citizen-dashboard.html    # Citizen Reports & Real-Time Resolution Tracker
│   ├── report-incident.html      # Damage Reporting Form (GPS + Photo Upload)
│   ├── officer-dashboard.html    # Command Center (Chart.js Analytics & KPI Feed)
│   ├── incidents.html            # Incident Triage Table (Filters & Search)
│   ├── incident-details.html     # Case Dossier (AI Analysis, Duplicates, Agent Triage)
│   ├── map.html                  # Interactive Map (Leaflet.js & Priority Markers)
│   ├── work-orders.html          # Work Order Lifecycle & Inspector Updates
│   ├── rag-assistant.html        # RAG Assistant (PDF Ingestion, Vector Search)
│   ├── admin-dashboard.html      # User Directory, Role Delegation & Audit Trail
│   └── assets/
│       ├── css/style.css         # Responsive SaaS Design System
│       └── js/                   # api.js, auth.js, ui.js
├── backend/                      # Java Spring Boot 3.3.5 Backend
│   ├── src/main/java/com/civicai/
│   │   ├── config/               # DotEnv & Application config
│   │   ├── controller/           # REST API Controllers (Auth, Incidents, WO, RAG)
│   │   ├── service/              # Core Business & AI Integration Logic
│   │   ├── repository/           # Spring Data JPA Repositories
│   │   ├── entity/               # 15 JPA Entity Models & Enums
│   │   ├── dto/                  # Request / Response Transfer Objects
│   │   ├── security/             # Stateless JWT & RBAC Filters
│   │   ├── exception/            # Global Exception Handling & Custom Exceptions
│   │   └── util/                 # Secure File Storage Service (UUID / Path Traversal)
│   ├── src/main/resources/
│   │   └── application.properties # Spring configuration with .env property overrides
│   └── pom.xml                   # Maven dependencies (Web, JPA, Security, MySQL, JJWT)
├── ai-service/                   # Python FastAPI AI Microservice
│   ├── app/
│   │   ├── main.py               # FastAPI entrypoint (Port 8000)
│   │   ├── routes/               # API Routers (analysis, rag, agent)
│   │   ├── services/             # Vision analysis & duplicate proximity engines
│   │   ├── rag/                  # PyMuPDF + Sentence Transformers + FAISS
│   │   └── agent/                # 8-Tool Agentic AI Incident Manager
│   ├── test_ai_service.py        # Automated test suite for AI services
│   ├── requirements.txt          # Python dependencies
│   └── .env.example              # AI Service environment template
├── database/                     # MySQL Relational Schema & Seed Data
│   ├── schema.sql                # Complete DDL for 14 tables, constraints, indexes
│   ├── seed.sql                  # Seed departments, demo users, realistic incidents
│   ├── setup_db.py               # Automated database execution script
│   └── generate_sample_pdfs.py   # Script to generate sample municipal SOP PDFs
├── uploads/                      # Secure file storage
│   ├── evidence/                 # Citizen photos & repair receipts
│   └── documents/                # Municipal maintenance SOP manuals (PDF)
├── docs/                         # Detailed architecture and workflow specifications
│   ├── ARCHITECTURE.md           # System data flow and diagrams
│   ├── API_DOCUMENTATION.md      # Complete REST API reference
│   ├── DATABASE.md               # Database schema & MySQL Workbench guide
│   ├── RAG_WORKFLOW.md           # FAISS RAG and prompt-injection defense
│   └── AGENT_WORKFLOW.md         # 8-step agentic workflow and tool allowlist
├── .env.example                  # Root environment variables template
├── SECURITY.md                   # Security architecture and vulnerability policy
└── README.md                     # Master project documentation
```

---

## ⚡ Quick Start Instructions (Windows & PowerShell)

### Step 1: Database Setup via MySQL Workbench
1. Open **MySQL Workbench** and connect to your local MySQL 8.0 server.
2. In MySQL Workbench, click **File > Open SQL Script...** (or `Ctrl+Shift+O`).
3. Select `d:\CIVIC AI\civicai\database\schema.sql` and execute (⚡ icon).
4. Select `d:\CIVIC AI\civicai\database\seed.sql` and execute (⚡ icon).
5. Refresh the schema list: confirm that **`civicai_db`** contains all 14 tables.

*Alternative automated PowerShell setup:*
```powershell
cd "d:\CIVIC AI\civicai\database"
py setup_db.py
```

### Step 2: Configure Environment Settings
Create a `.env` file in `civicai/` by copying `.env.example`:
```powershell
cd "d:\CIVIC AI\civicai"
Copy-Item .env.example .env
```
Open `.env` and set your MySQL password:
```env
DB_PASSWORD=your_actual_mysql_password
```

### Step 3: Start the Python AI Microservice
Open PowerShell Window 1:
```powershell
cd "d:\CIVIC AI\civicai\ai-service"
py -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```
Verify the health endpoint at: `http://localhost:8000/api/health`

### Step 4: Start the Java Spring Boot Backend
Open PowerShell Window 2:
```powershell
cd "d:\CIVIC AI\civicai\backend"
mvn spring-boot:run
```
Spring Boot starts on `http://localhost:8080`.

### Step 5: Launch the Frontend
Open `d:\CIVIC AI\civicai\frontend\index.html` directly in your browser or run a local web server:
```powershell
cd "d:\CIVIC AI\civicai\frontend"
py -m http.server 5500
```
Then visit: **`http://localhost:5500`** (or open `index.html` in Chrome/Edge).

---

## 👥 Demo Test Accounts

All pre-seeded test accounts share the initial password:
**`Password@123`**

| Role | Email | Dashboard Access |
|---|---|---|
| **CITIZEN** | `citizen@civicai.org` | Report damage, track personal submissions |
| **MUNICIPAL_OFFICER** | `officer@civicai.gov` | Triage incidents, run AI agent, approve work orders |
| **FIELD_INSPECTOR** | `inspector@civicai.gov` | View assigned work orders, update repair progress |
| **ADMIN** | `admin@civicai.gov` | User directory, role delegation, audit logs |

*(Note: The login page includes 1-click Quick Test buttons to instantly pre-fill any role!)*

---

## 🎬 Two-Minute Project Demonstration Script

Follow this script to deliver an end-to-end presentation of CivicAI:

1. **The Citizen Experience (0:00 - 0:35)**:
   - Go to `login.html`, click the **Citizen** quick button, and sign in.
   - Click **+ Report Damage**.
   - Select category **Pothole**, enter a description, click **📍 Use Current GPS Location**, attach a photo, and click **Submit**.
   - Note the generated reference number (e.g. `CIV-2026-XXXX`). Notice it appears immediately in the Citizen dashboard table with status `REPORTED`.

2. **Municipal Officer Triage & AI Intelligence (0:35 - 1:15)**:
   - Log out, click the **Officer** quick button, and sign in.
   - On the **Command Center**, view real-time Chart.js category and priority distributions.
   - Click **Triage Backlog** (`incidents.html`) and open the newly submitted incident case.
   - Click **Analyze Photo**: computer vision extracts asphalt cavity depth and calculates confidence.
   - Click **Check Proximity**: geospatial engine displays matching reports within 300 meters.
   - Click **🤖 Run Agentic AI Triage**: watch the autonomous agent execute all 8 tools sequentially, citing official municipal SOP manuals and staging a **Draft Work Order**.

3. **Work Order Approval & Field Resolution (1:15 - 1:45)**:
   - Click **Work Orders** in the sidebar.
   - The officer clicks **Approve Order** and then **Assign Inspector** (selects Marcus Cole).
   - Log out, sign in as **Inspector** (`inspector@civicai.gov`).
   - The inspector opens the work order, clicks **Update Progress**, selects **RESOLVED**, and enters field notes.
   - The incident automatically updates to **RESOLVED** across all dashboards!

4. **RAG Knowledge Assistant & City Map (1:45 - 2:00)**:
   - Open **RAG Assistant** (`rag-assistant.html`) and ask: *"What are the compaction and tack coat specifications for pothole repair?"*
   - See the grounded technical answer with exact document and page citations (`SOP_Road_Pothole_Repair_v2.pdf`, Page 4).
   - Open **City Map** (`map.html`): observe live color-coded priority pins across the urban grid.

---

## 🛡️ Security Architecture
- **Stateless JWT Tokens**: Signed using HMAC-SHA256 with user ID and roles.
- **BCrypt Password Hashing**: Passwords stored with work factor 12.
- **Server-Side RBAC**: `@PreAuthorize` method security protects backend endpoints.
- **UUID File Randomization**: Uploaded evidence filenames randomized to eliminate directory traversal.
- **RAG Prompt Injection Defense**: Untrusted PDF content sanitized and isolated before vectorization.
- **Human-in-the-Loop Constraint**: AI agent can stage draft work orders, but cannot self-approve or dispatch crews without officer authorization.

---

## 📤 GitHub Upload Instructions

To publish CivicAI to GitHub:

```powershell
cd "d:\CIVIC AI\civicai"
git init
git add .
git commit -m "feat: complete initial release of CivicAI damage intelligence platform"
git branch -M main
git remote add origin https://github.com/<your-username>/civicai.git
git push -u origin main
```
*(Sensitive files like `.env`, uploaded evidence, and build directories are automatically excluded by `.gitignore`.)*
