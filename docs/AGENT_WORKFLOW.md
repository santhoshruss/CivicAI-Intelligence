# CivicAI Agentic AI Incident Manager

## Overview
The Agentic AI Incident Manager coordinates the full lifecycle of an infrastructure incident from initial report triage to work order staging. It follows an autonomous tool-calling pattern while strictly enforcing the **Human-in-the-Loop** principle for municipal governance.

---

## Explicit Allowlist of 8 Agent Tools

| Step | Tool Name | Purpose | Output Summary |
|---|---|---|---|
| 1 | `get_incident_details` | Retrieves report metadata, coordinates, and description | Reference number, category, address |
| 2 | `analyze_damage` | Evaluates morphological image characteristics | Model name, predicted category, confidence score |
| 3 | `find_nearby_duplicates` | Scans 300m radius using Haversine formula | Number of nearby matching reports |
| 4 | `retrieve_maintenance_guidelines` | Queries RAG vector store for relevant city SOP | Document name, page citation, excerpt |
| 5 | `calculate_priority` | Combines severity, public safety, and report age | Calculated Priority (LOW, MEDIUM, HIGH, CRITICAL) |
| 6 | `generate_recommendation` | Synthesizes an actionable officer briefing | Formatted briefing with evidence and guidelines |
| 7 | `create_draft_work_order` | Stages work order linked to appropriate department | Work Order ID in `DRAFT` status |
| 8 | `generate_incident_summary` | Compiles audit log of the triage execution | Summary confirmation ready for review |

---

## Safety & Governance Safeguards
1. **No Self-Approval**: The agent is programmatically forbidden from approving work orders. Work orders are always saved in `DRAFT` status and require formal sign-off by a Municipal Officer.
2. **No Autonomous Dispatch**: Crew dispatch requires human authorization.
3. **Transparent Traceability**: Every tool execution is recorded in `agent_tool_logs` with timestamps, inputs, and outputs.
4. **No External Command Execution**: Tools have no shell access, no raw SQL execution, and operate within strict application boundaries.
