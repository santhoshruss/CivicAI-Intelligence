# CivicAI Database Setup & Architecture Guide

## Database: `civicai_db` (MySQL 8.0+)

### Overview
CivicAI uses a relational schema with 14 normalized tables designed for high-concurrency public reporting, evidence traceability, and automated agent workflows.

---

## MySQL Workbench Execution Instructions

Follow these exact steps in MySQL Workbench:

1. **Open MySQL Workbench** and connect to your local MySQL 8.0 instance (usually `localhost:3306` with user `root`).
2. **Open Schema Script**:
   - Go to `File` > `Open SQL Script...` (or press `Ctrl + Shift + O`).
   - Navigate to `d:\CIVIC AI\civicai\database\schema.sql`.
   - Click the **Execute** button (the lightning bolt icon ⚡) or press `Ctrl + Shift + Enter`.
   - Verify the output log at the bottom: You should see green checkmarks for `CREATE DATABASE` and all 14 `CREATE TABLE` statements.
3. **Open Seed Script**:
   - Go to `File` > `Open SQL Script...`.
   - Navigate to `d:\CIVIC AI\civicai\database\seed.sql`.
   - Click the **Execute** button (⚡) or press `Ctrl + Shift + Enter`.
   - Verify that 4 departments, seed accounts, initial incidents, and work orders were inserted.
4. **Verify Database**:
   - In the left **Schemas** pane, right-click and choose **Refresh All**.
   - Expand `civicai_db` > `Tables`.
   - Confirm all 14 tables are present:
     - `users`, `departments`, `incidents`, `evidence`, `incident_analysis`, `duplicate_incidents`, `maintenance_documents`, `work_orders`, `work_order_updates`, `agent_runs`, `agent_tool_logs`, `notifications`, `audit_logs`, `refresh_tokens`.
   - Right-click `users` and select **Select Rows - Limit 1000** to verify the seed users.

---

## Pre-Seeded Demonstration Accounts

All pre-seeded demo accounts share the initial password:
**`Password@123`**

| Role | Email | Purpose |
|------|-------|---------|
| **ADMIN** | `admin@civicai.gov` | Full access, user & department management, audit logs |
| **MUNICIPAL_OFFICER** | `officer@civicai.gov` | Incident triage, AI agent review, work order approvals |
| **FIELD_INSPECTOR** | `inspector@civicai.gov` | Assigned work order execution, inspection updates |
| **CITIZEN** | `citizen@civicai.org` | Report damage, view personal submissions |

---

## Alternative: Automated PowerShell Script

If you have your MySQL root password, you can also run the bundled script:
```powershell
cd "d:\CIVIC AI\civicai\database"
py setup_db.py
```
This script will prompt for your MySQL password (or read it from `civicai/.env`) and automatically execute `schema.sql` and `seed.sql`.
