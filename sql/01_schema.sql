-- ============================================================
-- 01_schema.sql
-- Description: Creates the database and all tables without complex constraints.
-- ============================================================

CREATE DATABASE IF NOT EXISTS hospital_bed_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE hospital_bed_db;

-- 1. WARDS TABLE
CREATE TABLE IF NOT EXISTS wards (
    ward_id         INT PRIMARY KEY AUTO_INCREMENT,
    ward_type       VARCHAR(20) NOT NULL,
    floor           INT NOT NULL,
    capacity        INT NOT NULL,
    daily_charge    DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB;

-- 2. BEDS TABLE
CREATE TABLE IF NOT EXISTS beds (
    bed_id            INT PRIMARY KEY AUTO_INCREMENT,
    ward_id           INT NOT NULL,
    bed_number        VARCHAR(10) NOT NULL,
    equipment_status  VARCHAR(100) DEFAULT 'Standard',
    current_status    VARCHAR(20) DEFAULT 'Available'
) ENGINE=InnoDB;

-- 3. PATIENTS TABLE
CREATE TABLE IF NOT EXISTS patients (
    patient_id        INT PRIMARY KEY AUTO_INCREMENT,
    name              VARCHAR(100) NOT NULL,
    age               INT,
    blood_group       VARCHAR(5),
    contact           VARCHAR(15) NOT NULL,
    address           VARCHAR(255),
    emergency_contact VARCHAR(15),
    registered_date   TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 4. STAFF TABLE
CREATE TABLE IF NOT EXISTS staff (
    staff_id   INT PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    role       VARCHAR(20) NOT NULL,
    department VARCHAR(50),
    phone      VARCHAR(15)
) ENGINE=InnoDB;

-- 5. ADMISSIONS TABLE
CREATE TABLE IF NOT EXISTS admissions (
    admission_id        INT PRIMARY KEY AUTO_INCREMENT,
    patient_id          INT NOT NULL,
    bed_id              INT NOT NULL,
    doctor_id           INT,
    admission_date      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_discharge  DATE,
    actual_discharge    TIMESTAMP NULL,
    status              VARCHAR(20) DEFAULT 'Active',
    notes               TEXT
) ENGINE=InnoDB;

-- 6. WAITLIST TABLE
CREATE TABLE IF NOT EXISTS waitlist (
    waitlist_id           INT PRIMARY KEY AUTO_INCREMENT,
    patient_id            INT NOT NULL,
    requested_ward_type   VARCHAR(20) NOT NULL,
    priority_score        INT NOT NULL DEFAULT 0,
    request_time          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status                VARCHAR(20) DEFAULT 'Waiting',
    notes                 TEXT
) ENGINE=InnoDB;

-- 7. BED MAINTENANCE LOG TABLE
CREATE TABLE IF NOT EXISTS bed_maintenance_log (
    log_id        INT PRIMARY KEY AUTO_INCREMENT,
    bed_id        INT NOT NULL,
    start_time    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time      TIMESTAMP NULL,
    reason        VARCHAR(255) NOT NULL,
    resolved_by   INT,
    status        VARCHAR(20) DEFAULT 'Ongoing'
) ENGINE=InnoDB;

-- 8. BED STATUS AUDIT TABLE
CREATE TABLE IF NOT EXISTS bed_status_audit (
    audit_id      INT PRIMARY KEY AUTO_INCREMENT,
    bed_id        INT NOT NULL,
    old_status    VARCHAR(20),
    new_status    VARCHAR(20) NOT NULL,
    changed_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by    VARCHAR(50),
    reason        VARCHAR(255)
) ENGINE=InnoDB;
