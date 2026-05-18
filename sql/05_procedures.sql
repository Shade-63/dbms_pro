-- ============================================================
-- 05_procedures.sql
-- Description: Creates stored procedures.
-- ============================================================

USE hospital_bed_db;

-- Procedure 1: Allocate Bed (Atomic Transaction)
DELIMITER //
CREATE PROCEDURE sp_allocate_bed(
    IN p_patient_id INT,
    IN p_ward_type VARCHAR(20),
    IN p_doctor_id INT,
    IN p_expected_discharge DATE,
    IN p_notes TEXT,
    OUT p_bed_id INT,
    OUT p_admission_id INT,
    OUT p_success BOOLEAN,
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_bed_id INT DEFAULT NULL;
    DECLARE v_admission_id INT DEFAULT NULL;

    START TRANSACTION;

    SELECT b.bed_id INTO v_bed_id
    FROM beds b
    JOIN wards w ON b.ward_id = w.ward_id
    WHERE w.ward_type = p_ward_type
      AND b.current_status = 'Available'
      AND b.equipment_status != 'Broken'
    ORDER BY b.bed_id
    LIMIT 1
    FOR UPDATE;

    IF v_bed_id IS NOT NULL THEN
        INSERT INTO admissions (
            patient_id, bed_id, doctor_id, 
            expected_discharge, status, notes
        ) VALUES (
            p_patient_id, v_bed_id, p_doctor_id,
            p_expected_discharge, 'Active', p_notes
        );

        SET v_admission_id = LAST_INSERT_ID();

        UPDATE beds 
        SET current_status = 'Occupied' 
        WHERE bed_id = v_bed_id;

        UPDATE waitlist 
        SET status = 'Allocated' 
        WHERE patient_id = p_patient_id 
          AND requested_ward_type = p_ward_type 
          AND status = 'Waiting';

        COMMIT;

        SET p_bed_id = v_bed_id;
        SET p_admission_id = v_admission_id;
        SET p_success = TRUE;
        SET p_message = CONCAT('Patient admitted to Bed ID: ', v_bed_id);
    ELSE
        INSERT INTO waitlist (
            patient_id, requested_ward_type, priority_score, status, notes
        ) VALUES (
            p_patient_id, p_ward_type, 100, 'Waiting', p_notes
        );

        COMMIT;

        SET p_bed_id = NULL;
        SET p_admission_id = NULL;
        SET p_success = FALSE;
        SET p_message = 'No bed available. Patient added to waitlist.';
    END IF;
END//
DELIMITER ;

-- Procedure 2: Discharge Patient
DELIMITER //
CREATE PROCEDURE sp_discharge_patient(
    IN p_admission_id INT,
    IN p_maintenance_needed BOOLEAN,
    IN p_maintenance_reason VARCHAR(255),
    OUT p_message VARCHAR(255)
)
BEGIN
    DECLARE v_bed_id INT;
    DECLARE v_ward_id INT;
    DECLARE v_ward_type VARCHAR(20);
    DECLARE v_patient_name VARCHAR(100);
    DECLARE v_waitlist_patient_id INT;
    DECLARE v_waitlist_id INT;
    DECLARE v_new_admission_id INT;
    DECLARE v_auto_allocated BOOLEAN DEFAULT FALSE;
    DECLARE v_waitlist_found BOOLEAN DEFAULT TRUE;

    DECLARE waitlist_cursor CURSOR FOR
        SELECT w.waitlist_id, w.patient_id
        FROM waitlist w
        WHERE w.requested_ward_type = v_ward_type
          AND w.status = 'Waiting'
        ORDER BY w.priority_score DESC, w.request_time ASC
        LIMIT 1;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_waitlist_found = FALSE;

    START TRANSACTION;

    SELECT 
        a.bed_id, b.ward_id, w.ward_type, p.name
    INTO 
        v_bed_id, v_ward_id, v_ward_type, v_patient_name
    FROM admissions a
    JOIN beds b ON a.bed_id = b.bed_id
    JOIN wards w ON b.ward_id = w.ward_id
    JOIN patients p ON a.patient_id = p.patient_id
    WHERE a.admission_id = p_admission_id 
      AND a.status = 'Active';

    IF v_bed_id IS NULL THEN
        ROLLBACK;
        SET p_message = 'Error: Active admission not found';
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Active admission not found';
    END IF;

    UPDATE admissions 
    SET status = 'Discharged', 
        actual_discharge = NOW() 
    WHERE admission_id = p_admission_id;

    IF p_maintenance_needed THEN
        SET @status_change_reason = 'Post-Discharge Maintenance';
        UPDATE beds 
        SET current_status = 'Maintenance' 
        WHERE bed_id = v_bed_id;

        INSERT INTO bed_maintenance_log (bed_id, reason, status)
        VALUES (v_bed_id, COALESCE(p_maintenance_reason, 'Post-discharge cleaning'), 'Ongoing');
        SET @status_change_reason = NULL;

        COMMIT;
        SET p_message = CONCAT(v_patient_name, ' discharged. Bed marked for maintenance.');
    ELSE
        UPDATE beds 
        SET current_status = 'Available' 
        WHERE bed_id = v_bed_id;

        OPEN waitlist_cursor;
        FETCH waitlist_cursor INTO v_waitlist_id, v_waitlist_patient_id;
        CLOSE waitlist_cursor;

        IF v_waitlist_found AND v_waitlist_id IS NOT NULL THEN
            UPDATE waitlist 
            SET status = 'Allocated' 
            WHERE waitlist_id = v_waitlist_id;

            INSERT INTO admissions (
                patient_id, bed_id, status, admission_date, notes
            ) VALUES (
                v_waitlist_patient_id, v_bed_id, 'Active', NOW(),
                'Auto-allocated from waitlist'
            );
            SET v_new_admission_id = LAST_INSERT_ID();
            SET v_auto_allocated = TRUE;
        END IF;

        COMMIT;

        IF v_auto_allocated THEN
            SET p_message = CONCAT(v_patient_name, ' discharged. Bed auto-allocated to waitlisted patient.');
        ELSE
            SET p_message = CONCAT(v_patient_name, ' discharged. Bed is now available.');
        END IF;
    END IF;
END//
DELIMITER ;

-- Procedure 3: Process Waitlist
DELIMITER //
CREATE PROCEDURE sp_process_waitlist(
    OUT p_allocated_count INT,
    OUT p_remaining_count INT
)
BEGIN
    DECLARE v_waitlist_id INT;
    DECLARE v_patient_id INT;
    DECLARE v_ward_type VARCHAR(20);
    DECLARE v_bed_id INT;
    DECLARE v_done BOOLEAN DEFAULT FALSE;
    DECLARE v_allocated INT DEFAULT 0;

    DECLARE waitlist_cursor CURSOR FOR
        SELECT waitlist_id, patient_id, requested_ward_type
        FROM waitlist
        WHERE status = 'Waiting'
        ORDER BY priority_score DESC, request_time ASC;

    DECLARE CONTINUE HANDLER FOR NOT FOUND
        SET v_done = TRUE;

    START TRANSACTION;
    OPEN waitlist_cursor;

    read_loop: LOOP
        FETCH waitlist_cursor INTO v_waitlist_id, v_patient_id, v_ward_type;
        IF v_done THEN
            LEAVE read_loop;
        END IF;

        SET v_bed_id = NULL;

        SELECT b.bed_id INTO v_bed_id
        FROM beds b
        JOIN wards w ON b.ward_id = w.ward_id
        WHERE w.ward_type = v_ward_type
          AND b.current_status = 'Available'
          AND b.equipment_status != 'Broken'
        LIMIT 1
        FOR UPDATE;

        IF v_bed_id IS NOT NULL THEN
            UPDATE waitlist 
            SET status = 'Allocated' 
            WHERE waitlist_id = v_waitlist_id;

            INSERT INTO admissions (
                patient_id, bed_id, status, admission_date, notes
            ) VALUES (
                v_patient_id, v_bed_id, 'Active', NOW(),
                'Auto-allocated from waitlist batch process'
            );

            UPDATE beds 
            SET current_status = 'Occupied' 
            WHERE bed_id = v_bed_id;

            SET v_allocated = v_allocated + 1;
        END IF;
    END LOOP;

    CLOSE waitlist_cursor;
    COMMIT;

    SET p_allocated_count = v_allocated;
    SELECT COUNT(*) INTO p_remaining_count
    FROM waitlist
    WHERE status = 'Waiting';
END//
DELIMITER ;

-- Procedure 4: Update Priority
DELIMITER //
CREATE PROCEDURE sp_update_waitlist_priority()
BEGIN
    UPDATE waitlist
    SET priority_score = 
        CASE 
            WHEN priority_score < 100 THEN 100
            ELSE priority_score
        END + (TIMESTAMPDIFF(HOUR, request_time, NOW()) * 10)
    WHERE status = 'Waiting';
END//
DELIMITER ;
