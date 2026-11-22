-- ==============================================================================
-- CivicAI Database Migration v2.0 Rollback Script
-- Reverts v2.0 schema alterations safely.
-- ==============================================================================

USE civicai_db;

ALTER TABLE incidents
  DROP COLUMN IF EXISTS original_language,
  DROP COLUMN IF EXISTS normalized_description;

ALTER TABLE incident_analysis
  DROP COLUMN IF EXISTS detected_objects,
  DROP COLUMN IF EXISTS model_version,
  DROP COLUMN IF EXISTS is_heuristic;

ALTER TABLE duplicate_incidents
  DROP COLUMN IF EXISTS similarity_level,
  DROP COLUMN IF EXISTS explanation;

ALTER TABLE maintenance_documents
  DROP COLUMN IF EXISTS ocr_extracted,
  DROP COLUMN IF EXISTS document_metadata;

ALTER TABLE agent_runs
  DROP COLUMN IF EXISTS model_versions;

DROP INDEX IF EXISTS idx_incidents_cat_loc ON incidents;
