-- ==============================================================================
-- CivicAI Database Schema
-- Database: civicai_db (MySQL 8.0+)
-- ==============================================================================

CREATE DATABASE IF NOT EXISTS civicai_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE civicai_db;

-- ------------------------------------------------------------------------------
-- 1. Departments Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    code VARCHAR(30) NOT NULL UNIQUE,
    description TEXT,
    contact_email VARCHAR(150),
    contact_phone VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 2. Users Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'CITIZEN', -- CITIZEN, MUNICIPAL_OFFICER, FIELD_INSPECTOR, ADMIN
    department_id BIGINT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_department FOREIGN KEY (department_id) 
        REFERENCES departments(id) ON DELETE SET NULL,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 3. Incidents Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference_number VARCHAR(50) NOT NULL UNIQUE,
    citizen_id BIGINT NOT NULL,
    category VARCHAR(50) NOT NULL, -- POTHOLE, ROAD_CRACK, STREETLIGHT_DAMAGE, GARBAGE_OVERFLOW, WATER_LEAKAGE
    description TEXT NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    address VARCHAR(255),
    status VARCHAR(40) NOT NULL DEFAULT 'REPORTED', -- REPORTED, UNDER_REVIEW, AI_ANALYZED, WORK_ORDER_CREATED, IN_PROGRESS, RESOLVED, REJECTED
    recommended_priority VARCHAR(20) DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, CRITICAL
    approved_priority VARCHAR(20) DEFAULT 'MEDIUM',
    assigned_department_id BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_incidents_citizen FOREIGN KEY (citizen_id) 
        REFERENCES users(id) ON DELETE RESTRICT,
    CONSTRAINT fk_incidents_department FOREIGN KEY (assigned_department_id) 
        REFERENCES departments(id) ON DELETE SET NULL,
    INDEX idx_incidents_ref (reference_number),
    INDEX idx_incidents_status (status),
    INDEX idx_incidents_category (category),
    INDEX idx_incidents_priority (approved_priority),
    INDEX idx_incidents_citizen (citizen_id),
    INDEX idx_incidents_location (latitude, longitude),
    INDEX idx_incidents_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 4. Evidence Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evidence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    uploaded_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_evidence_incident FOREIGN KEY (incident_id) 
        REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_evidence_uploader FOREIGN KEY (uploaded_by) 
        REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_evidence_incident (incident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 5. Incident Analysis Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS incident_analysis (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    model_name VARCHAR(100) NOT NULL,
    predicted_category VARCHAR(50) NOT NULL,
    confidence DOUBLE NOT NULL,
    analysis_status VARCHAR(40) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, PENDING, FAILED, UNAVAILABLE
    explanation TEXT,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_analysis_incident FOREIGN KEY (incident_id) 
        REFERENCES incidents(id) ON DELETE CASCADE,
    INDEX idx_analysis_incident (incident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 6. Duplicate Incidents Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS duplicate_incidents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    related_incident_id BIGINT NOT NULL,
    distance_meters DOUBLE NOT NULL,
    similarity_score DOUBLE NOT NULL,
    review_status VARCHAR(40) DEFAULT 'POSSIBLE_DUPLICATE', -- POSSIBLE_DUPLICATE, CONFIRMED_DUPLICATE, DISMISSED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_duplicates_incident FOREIGN KEY (incident_id) 
        REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_duplicates_related FOREIGN KEY (related_incident_id) 
        REFERENCES incidents(id) ON DELETE CASCADE,
    INDEX idx_duplicates_incident (incident_id),
    INDEX idx_duplicates_related (related_incident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 7. Maintenance Documents (RAG Knowledge Base) Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS maintenance_documents (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    category VARCHAR(50) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    chunk_count INT DEFAULT 0,
    indexed_status VARCHAR(30) DEFAULT 'INDEXED',
    uploaded_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_docs_uploader FOREIGN KEY (uploaded_by) 
        REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_docs_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 8. Work Orders Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    assigned_inspector_id BIGINT NULL,
    department_id BIGINT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    description TEXT,
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT', -- DRAFT, PENDING_APPROVAL, APPROVED, ASSIGNED, IN_PROGRESS, COMPLETED_PENDING_VERIFICATION, RESOLVED, REJECTED
    approved_by BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_work_orders_incident FOREIGN KEY (incident_id) 
        REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_work_orders_inspector FOREIGN KEY (assigned_inspector_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_work_orders_department FOREIGN KEY (department_id) 
        REFERENCES departments(id) ON DELETE SET NULL,
    CONSTRAINT fk_work_orders_approver FOREIGN KEY (approved_by) 
        REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_work_orders_incident (incident_id),
    INDEX idx_work_orders_status (status),
    INDEX idx_work_orders_inspector (assigned_inspector_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 9. Work Order Updates Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS work_order_updates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    updated_by BIGINT NOT NULL,
    previous_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    notes TEXT,
    evidence_filename VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wo_updates_workorder FOREIGN KEY (work_order_id) 
        REFERENCES work_orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_wo_updates_user FOREIGN KEY (updated_by) 
        REFERENCES users(id) ON DELETE RESTRICT,
    INDEX idx_wo_updates_wo (work_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 10. Agent Runs Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_runs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    incident_id BIGINT NOT NULL,
    execution_status VARCHAR(40) NOT NULL DEFAULT 'COMPLETED', -- COMPLETED, RUNNING, FAILED
    recommendation TEXT,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL,
    CONSTRAINT fk_agent_runs_incident FOREIGN KEY (incident_id) 
        REFERENCES incidents(id) ON DELETE CASCADE,
    INDEX idx_agent_runs_incident (incident_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 11. Agent Tool Logs Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS agent_tool_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    agent_run_id BIGINT NOT NULL,
    tool_name VARCHAR(100) NOT NULL,
    input_summary TEXT,
    output_summary TEXT,
    execution_status VARCHAR(30) DEFAULT 'SUCCESS',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_agent_tools_run FOREIGN KEY (agent_run_id) 
        REFERENCES agent_runs(id) ON DELETE CASCADE,
    INDEX idx_agent_tools_run (agent_run_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 12. Notifications Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_notifications_user (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 13. Audit Logs Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    entity_name VARCHAR(100) NOT NULL,
    entity_id BIGINT NULL,
    details TEXT,
    ip_address VARCHAR(45) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_audit_user (user_id),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------------------------
-- 14. Refresh Tokens Table
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) 
        REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_refresh_token_lookup (token)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
