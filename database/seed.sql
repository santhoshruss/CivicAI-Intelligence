-- ==============================================================================
-- CivicAI Seed Data
-- ==============================================================================

USE civicai_db;

-- 1. Seed Municipal Departments
INSERT INTO departments (id, name, code, description, contact_email, contact_phone) VALUES
(1, 'Roads & Infrastructure Maintenance', 'ROADS_INFRA', 'Responsible for asphalt repair, potholes, road cracks, sidewalks and traffic calming structures.', 'roads@civicai.gov', '+1-555-0101'),
(2, 'Electrical & Public Lighting', 'ELEC_LIGHT', 'Maintains streetlights, traffic signals, junctions and public power conduits.', 'lighting@civicai.gov', '+1-555-0102'),
(3, 'Water Supply & Drainage Authority', 'WATER_DRAIN', 'Handles potable pipe bursts, main line leaks, storm drainage blocks and sewer overflows.', 'water@civicai.gov', '+1-555-0103'),
(4, 'Solid Waste Management', 'WASTE_MGMT', 'Oversees public garbage bins, community cleanup, recycling and illegal dumping eradication.', 'waste@civicai.gov', '+1-555-0104')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. Seed Initial Users (Password: Password@123)
-- Valid BCrypt hash for 'Password@123': $2a$10$K8ZbPArje9UGPn6MIzwt/.jmGxQ84qRdj98y7chzBuX0.dmigBLFG

INSERT INTO users (id, name, email, password_hash, role, department_id, enabled) VALUES
(1, 'City Administrator', 'admin@civicai.gov', '$2a$10$K8ZbPArje9UGPn6MIzwt/.jmGxQ84qRdj98y7chzBuX0.dmigBLFG', 'ADMIN', 1, TRUE),
(2, 'Chief Officer Elena Vance', 'officer@civicai.gov', '$2a$10$K8ZbPArje9UGPn6MIzwt/.jmGxQ84qRdj98y7chzBuX0.dmigBLFG', 'MUNICIPAL_OFFICER', 1, TRUE),
(3, 'Field Inspector Marcus Cole', 'inspector@civicai.gov', '$2a$10$K8ZbPArje9UGPn6MIzwt/.jmGxQ84qRdj98y7chzBuX0.dmigBLFG', 'FIELD_INSPECTOR', 1, TRUE),
(4, 'Sarah Jenkins (Citizen)', 'citizen@civicai.org', '$2a$10$K8ZbPArje9UGPn6MIzwt/.jmGxQ84qRdj98y7chzBuX0.dmigBLFG', 'CITIZEN', NULL, TRUE)
ON DUPLICATE KEY UPDATE password_hash=VALUES(password_hash), email=VALUES(email);

-- 3. Seed Initial Realistic Incidents for Demonstration
INSERT INTO incidents (id, reference_number, citizen_id, category, description, latitude, longitude, address, status, recommended_priority, approved_priority, assigned_department_id) VALUES
(1, 'CIV-2026-0001', 4, 'POTHOLE', 'Severe pothole measuring approximately 40cm across on the southbound lane near crosswalk. Vehicles are swerving sharply to avoid tire damage.', 37.774929, -122.419416, 'Market St & 7th St, Downtown', 'REPORTED', 'HIGH', 'HIGH', 1),
(2, 'CIV-2026-0002', 4, 'STREETLIGHT_DAMAGE', 'Streetlight pole #L-42 shattered cover with exposed electrical cabling hanging within reach of pedestrians.', 37.775830, -122.418210, 'Civic Center Plaza East', 'UNDER_REVIEW', 'CRITICAL', 'CRITICAL', 2),
(3, 'CIV-2026-0003', 4, 'WATER_LEAKAGE', 'High-pressure potable water main bubbling through street asphalt pavement. Water stream pooling along gutter curb.', 37.772540, -122.421950, '142 Mission St & 8th St', 'WORK_ORDER_CREATED', 'HIGH', 'HIGH', 3),
(4, 'CIV-2026-0004', 4, 'GARBAGE_OVERFLOW', 'Municipal trash bin overflowed with municipal bags torn by wind, blocking pedestrian sidewalk access.', 37.778200, -122.415300, '350 Golden Gate Ave', 'RESOLVED', 'LOW', 'LOW', 4)
ON DUPLICATE KEY UPDATE reference_number=VALUES(reference_number);

-- 4. Seed Initial Evidence Records
INSERT INTO evidence (id, incident_id, stored_filename, original_filename, content_type, file_size, uploaded_by) VALUES
(1, 1, 'sample_pothole_01.jpg', 'pothole_market_st.jpg', 'image/jpeg', 248900, 4),
(2, 2, 'sample_streetlight_02.jpg', 'damaged_pole_wire.jpg', 'image/jpeg', 184200, 4),
(3, 3, 'sample_water_leak_03.jpg', 'water_main_rupture.jpg', 'image/jpeg', 312500, 4)
ON DUPLICATE KEY UPDATE stored_filename=VALUES(stored_filename);

-- 5. Seed Work Order for Incident 3
INSERT INTO work_orders (id, incident_id, assigned_inspector_id, department_id, priority, description, status, approved_by) VALUES
(1, 3, 3, 3, 'HIGH', 'Dispatched urgent pipe repair crew with pressure sensor diagnostics. Excavate surface layer and seal ruptured coupling.', 'ASSIGNED', 2)
ON DUPLICATE KEY UPDATE id=VALUES(id);

-- 6. Seed Work Order Update
INSERT INTO work_order_updates (id, work_order_id, updated_by, previous_status, new_status, notes) VALUES
(1, 1, 2, 'DRAFT', 'APPROVED', 'Officer reviewed severity. Approved emergency response allocation.'),
(2, 1, 2, 'APPROVED', 'ASSIGNED', 'Assigned to Field Inspector Marcus Cole for immediate on-site excavation.')
ON DUPLICATE KEY UPDATE id=VALUES(id);

-- 7. Seed Sample Maintenance Knowledge Document (RAG)
INSERT INTO maintenance_documents (id, title, category, stored_filename, original_filename, file_size, chunk_count, indexed_status, uploaded_by) VALUES
(1, 'Municipal Road & Pothole Repair Standard Operating Procedure (SOP-RD-04)', 'ROADS_INFRA', 'sop_road_repair_v2.pdf', 'SOP_Road_Pothole_Repair_v2.pdf', 524288, 12, 'INDEXED', 1),
(2, 'Public Electrical & Streetlight Emergency Response Protocol (SOP-EL-09)', 'ELEC_LIGHT', 'sop_electrical_lighting.pdf', 'SOP_Electrical_Lighting_Safety.pdf', 412900, 9, 'INDEXED', 1),
(3, 'Water Infrastructure Pipeline Rupture Quick Response (SOP-WT-01)', 'WATER_DRAIN', 'sop_water_leakage.pdf', 'SOP_Water_Infrastructure_Rupture.pdf', 389100, 8, 'INDEXED', 1)
ON DUPLICATE KEY UPDATE id=VALUES(id);

-- 8. Seed Initial Notifications
INSERT INTO notifications (id, user_id, title, message, is_read) VALUES
(1, 4, 'Incident Submitted Successfully', 'Your report CIV-2026-0001 (POTHOLE) has been received and queued for AI analysis.', FALSE),
(2, 2, 'New High Priority Incident', 'Incident CIV-2026-0002 flagged as CRITICAL priority near Civic Center Plaza.', FALSE),
(3, 3, 'Work Order Assigned', 'You have been assigned to Work Order #1 for water main leakage inspection on Mission St.', FALSE)
ON DUPLICATE KEY UPDATE id=VALUES(id);
