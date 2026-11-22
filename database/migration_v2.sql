-- ==============================================================================
-- CivicAI Database Migration v2.0
-- Adds Hugging Face AI enhancements: Multilingual NLP, Detected Objects,
-- ML Priority Metadata, and OCR Document Tracking.
-- Preserves all existing tables and data.
-- ==============================================================================

USE civicai_db;

-- 1. Incidents Table: Add language and normalized text tracking
ALTER TABLE incidents
  ADD COLUMN IF NOT EXISTS original_language VARCHAR(20) DEFAULT 'English',
  ADD COLUMN IF NOT EXISTS normalized_description TEXT NULL;

-- 2. Incident Analysis Table: Add detected objects, model versioning, heuristic flag
ALTER TABLE incident_analysis
  ADD COLUMN IF NOT EXISTS detected_objects JSON NULL,
  ADD COLUMN IF NOT EXISTS model_version VARCHAR(100) NULL,
  ADD COLUMN IF NOT EXISTS is_heuristic BOOLEAN DEFAULT FALSE;

-- 3. Duplicate Incidents Table: Add similarity classification level and rationale
ALTER TABLE duplicate_incidents
  ADD COLUMN IF NOT EXISTS similarity_level VARCHAR(30) DEFAULT 'MEDIUM_SIMILARITY',
  ADD COLUMN IF NOT EXISTS explanation TEXT NULL;

-- 4. Maintenance Documents Table: Track OCR extraction and structured metadata
ALTER TABLE maintenance_documents
  ADD COLUMN IF NOT EXISTS ocr_extracted BOOLEAN DEFAULT FALSE,
  ADD COLUMN IF NOT EXISTS document_metadata JSON NULL;

-- 5. Agent Runs Table: Track models and multi-tool execution telemetry
ALTER TABLE agent_runs
  ADD COLUMN IF NOT EXISTS model_versions JSON NULL;

-- Index optimization for geospatial and category duplicate filtering
CREATE INDEX IF NOT EXISTS idx_incidents_cat_loc ON incidents (category, latitude, longitude);
