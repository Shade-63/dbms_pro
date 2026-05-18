-- ============================================================
-- 04_triggers.sql
-- Description: Creates all database triggers.
-- ============================================================

USE hospital_bed_db;

-- 1. Prevent Multiple Active Admissions Per Bed (Assertion Equivalent)
DELIMITER //
CREATE TRIGGER trg_prevent_double_active_admission
BEFORE INSERT ON admissions
FOR EACH ROW
BEGIN
    DECLARE v_active_count INT;
    IF NEW.status = 'Active' THEN
        SELECT COUNT(*) INTO v_active_count
        FROM admissions
        WHERE bed_id = NEW.bed_id 
          AND status = 'Active';
        IF v_active_count > 0 THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Bed is already occupied by an active admission';
        END IF;
    END IF;
END//
DELIMITER ;

-- 2. Audit Log on Bed Status Change
DELIMITER //
CREATE TRIGGER trg_audit_bed_status_update
AFTER UPDATE ON beds
FOR EACH ROW
BEGIN
    IF OLD.current_status != NEW.current_status THEN
        INSERT INTO bed_status_audit (
            bed_id, old_status, new_status, changed_by, reason
        ) VALUES (
            NEW.bed_id, OLD.current_status, NEW.current_status,
            COALESCE(@session_user, 'SYSTEM'),
            COALESCE(@status_change_reason, 'Manual Update')
        );
    END IF;
END//
DELIMITER ;

-- 3. Auto-Update Bed Status on Admission Insert
DELIMITER //
CREATE TRIGGER trg_bed_occupied_on_admit
AFTER INSERT ON admissions
FOR EACH ROW
BEGIN
    IF NEW.status = 'Active' THEN
        UPDATE beds 
        SET current_status = 'Occupied' 
        WHERE bed_id = NEW.bed_id;
    END IF;
END//
DELIMITER ;

-- 4. Auto-Update Bed Status on Discharge
DELIMITER //
CREATE TRIGGER trg_bed_available_on_discharge
AFTER UPDATE ON admissions
FOR EACH ROW
BEGIN
    IF OLD.status = 'Active' AND NEW.status = 'Discharged' THEN
        SET @status_change_reason = 'Patient Discharge';
        UPDATE beds 
        SET current_status = 'Available' 
        WHERE bed_id = NEW.bed_id;
        SET @status_change_reason = NULL;
    END IF;
END//
DELIMITER ;

-- 5. Prevent Maintenance Start on Occupied Bed
DELIMITER //
CREATE TRIGGER trg_prevent_maint_occupied
BEFORE INSERT ON bed_maintenance_log
FOR EACH ROW
BEGIN
    DECLARE v_bed_status VARCHAR(20);
    SELECT current_status INTO v_bed_status
    FROM beds 
    WHERE bed_id = NEW.bed_id;
    IF v_bed_status = 'Occupied' THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Error: Cannot start maintenance on an occupied bed';
    END IF;
END//
DELIMITER ;

-- 6. Enforce Ward Capacity (Assertion Equivalent)
DELIMITER //
CREATE TRIGGER trg_enforce_ward_capacity
BEFORE INSERT ON admissions
FOR EACH ROW
BEGIN
    DECLARE v_capacity INT;
    DECLARE v_current_occupied INT;
    DECLARE v_ward_id INT;

    -- Only check if admitting as 'Active'
    IF NEW.status = 'Active' THEN
        SELECT w.ward_id, w.capacity INTO v_ward_id, v_capacity
        FROM beds b
        JOIN wards w ON b.ward_id = w.ward_id
        WHERE b.bed_id = NEW.bed_id;

        SELECT COUNT(*) INTO v_current_occupied
        FROM admissions a
        JOIN beds b ON a.bed_id = b.bed_id
        WHERE b.ward_id = v_ward_id AND a.status = 'Active';

        IF v_current_occupied >= v_capacity THEN
            SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Error: Ward capacity exceeded. Cannot admit more patients.';
        END IF;
    END IF;
END//
DELIMITER ;
