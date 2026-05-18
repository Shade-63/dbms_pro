-- ============================================================
-- 08_test_queries.sql
-- Description: Validation queries for testing the database.
-- ============================================================

USE hospital_bed_db;

-- 1. Check all active admissions
SELECT * FROM vw_active_admissions;

-- 2. Check current occupancy
SELECT * FROM vw_current_occupancy;

-- 3. Check available beds
SELECT * FROM vw_available_beds;

-- 4. Check waitlist
SELECT * FROM vw_waitlist_details;

-- 5. Test constraint: Try to admit to an occupied bed (Should fail)
-- INSERT INTO admissions (patient_id, bed_id, doctor_id, status) VALUES (1, 3, 2, 'Active');

-- 6. Test trigger: Update bed status manually (Check audit log)
-- UPDATE beds SET equipment_status = 'New Standard' WHERE bed_id = 1;
-- SELECT * FROM bed_status_audit;
