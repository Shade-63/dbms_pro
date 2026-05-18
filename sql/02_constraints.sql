-- ============================================================
-- 02_constraints.sql
-- Description: Adds FOREIGN KEY, CHECK, and UNIQUE constraints to all tables.
-- ============================================================

USE hospital_bed_db;

-- 1. WARDS CONSTRAINTS
ALTER TABLE wards
    ADD CONSTRAINT chk_ward_type CHECK (ward_type IN ('General','ICU','Emergency','Private')),
    ADD CONSTRAINT chk_ward_floor CHECK (floor > 0 AND floor <= 20),
    ADD CONSTRAINT chk_ward_capacity CHECK (capacity > 0 AND capacity <= 100),
    ADD CONSTRAINT chk_ward_charge CHECK (daily_charge >= 0),
    ADD CONSTRAINT uq_ward_type_floor UNIQUE (ward_type, floor);

-- 2. BEDS CONSTRAINTS
ALTER TABLE beds
    ADD CONSTRAINT fk_bed_ward FOREIGN KEY (ward_id) REFERENCES wards(ward_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT chk_bed_status CHECK (current_status IN ('Available','Occupied','Maintenance')),
    ADD CONSTRAINT chk_equipment CHECK (equipment_status IS NOT NULL),
    ADD CONSTRAINT uq_bed_ward_number UNIQUE (ward_id, bed_number);

-- 3. PATIENTS CONSTRAINTS
ALTER TABLE patients
    ADD CONSTRAINT chk_patient_age CHECK (age >= 0 AND age <= 150),
    ADD CONSTRAINT chk_patient_name CHECK (CHAR_LENGTH(name) >= 2),
    ADD CONSTRAINT chk_blood_group CHECK (blood_group IN ('A+','A-','B+','B-','O+','O-','AB+','AB-') OR blood_group IS NULL),
    ADD CONSTRAINT chk_patient_contact CHECK (contact REGEXP '^[0-9]{10}$'),
    ADD CONSTRAINT uq_patient_contact UNIQUE (contact);

-- 4. STAFF CONSTRAINTS
ALTER TABLE staff
    ADD CONSTRAINT chk_staff_role CHECK (role IN ('Doctor','Nurse','Admin')),
    ADD CONSTRAINT chk_staff_name CHECK (CHAR_LENGTH(name) >= 2),
    ADD CONSTRAINT chk_staff_phone CHECK (phone REGEXP '^[0-9]{10}$' OR phone IS NULL);

-- 5. ADMISSIONS CONSTRAINTS
ALTER TABLE admissions
    ADD CONSTRAINT fk_adm_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_adm_bed FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON DELETE RESTRICT ON UPDATE CASCADE,
    ADD CONSTRAINT fk_adm_doctor FOREIGN KEY (doctor_id) REFERENCES staff(staff_id) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT chk_adm_status CHECK (status IN ('Active','Discharged','Transferred')),
    ADD CONSTRAINT chk_adm_dates CHECK (expected_discharge IS NULL OR expected_discharge >= DATE(admission_date));

-- Note: The UNIQUE constraint on active admissions is handled via a trigger, as MySQL 
-- doesn't fully support partial UNIQUE constraints (e.g. bed_id where status='Active').

-- 6. WAITLIST CONSTRAINTS
ALTER TABLE waitlist
    ADD CONSTRAINT fk_wl_patient FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT chk_wl_status CHECK (status IN ('Waiting','Allocated','Cancelled')),
    ADD CONSTRAINT chk_wl_ward CHECK (requested_ward_type IN ('General','ICU','Emergency','Private')),
    ADD CONSTRAINT chk_wl_priority CHECK (priority_score >= 0),
    ADD CONSTRAINT uq_wl_patient_ward UNIQUE (patient_id, requested_ward_type, status);

-- 7. BED MAINTENANCE LOG CONSTRAINTS
ALTER TABLE bed_maintenance_log
    ADD CONSTRAINT fk_maint_bed FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT fk_maint_staff FOREIGN KEY (resolved_by) REFERENCES staff(staff_id) ON DELETE SET NULL ON UPDATE CASCADE,
    ADD CONSTRAINT chk_maint_status CHECK (status IN ('Ongoing','Completed','Cancelled')),
    ADD CONSTRAINT chk_maint_dates CHECK (end_time IS NULL OR end_time >= start_time),
    ADD CONSTRAINT chk_maint_reason CHECK (CHAR_LENGTH(reason) >= 3);

-- 8. BED STATUS AUDIT CONSTRAINTS
ALTER TABLE bed_status_audit
    ADD CONSTRAINT fk_audit_bed FOREIGN KEY (bed_id) REFERENCES beds(bed_id) ON DELETE CASCADE ON UPDATE CASCADE,
    ADD CONSTRAINT chk_audit_old CHECK (old_status IN ('Available','Occupied','Maintenance') OR old_status IS NULL),
    ADD CONSTRAINT chk_audit_new CHECK (new_status IN ('Available','Occupied','Maintenance'));
