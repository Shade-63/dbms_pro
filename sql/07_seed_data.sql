-- ============================================================
-- 07_seed_data.sql
-- Description: Inserts sample data for testing.
-- ============================================================

USE hospital_bed_db;

-- Insert Wards
INSERT INTO wards (ward_type, floor, capacity, daily_charge) VALUES
('General', 1, 10, 500.00),
('ICU', 2, 5, 2500.00),
('Emergency', 1, 8, 1000.00),
('Private', 3, 6, 1500.00);

-- Insert Beds (20 total)
INSERT INTO beds (ward_id, bed_number, equipment_status, current_status) VALUES
(1, 'G-101', 'Standard', 'Available'),
(1, 'G-102', 'Standard', 'Available'),
(1, 'G-103', 'Standard', 'Occupied'),
(1, 'G-104', 'Standard', 'Available'),
(2, 'ICU-01', 'Ventilator, Monitor', 'Available'),
(2, 'ICU-02', 'Ventilator', 'Occupied'),
(2, 'ICU-03', 'Monitor', 'Available'),
(2, 'ICU-04', 'Ventilator, Monitor', 'Maintenance'),
(2, 'ICU-05', 'Standard', 'Available'),
(3, 'E-101', 'Oxygen', 'Available'),
(3, 'E-102', 'Oxygen, Monitor', 'Occupied'),
(3, 'E-103', 'Standard', 'Available'),
(3, 'E-104', 'Oxygen', 'Available'),
(4, 'P-301', 'Premium', 'Available'),
(4, 'P-302', 'Premium', 'Occupied'),
(4, 'P-303', 'Premium', 'Available'),
(4, 'P-304', 'Premium', 'Available'),
(4, 'P-305', 'Premium', 'Maintenance'),
(4, 'P-306', 'Premium', 'Available');

-- Insert Staff
INSERT INTO staff (name, role, department, phone) VALUES
('Dr. Rajesh Sharma', 'Doctor', 'Cardiology', '9876543210'),
('Dr. Priya Patel', 'Doctor', 'General Medicine', '9876543211'),
('Dr. Amit Verma', 'Doctor', 'Emergency', '9876543212'),
('Dr. Sunita Rao', 'Doctor', 'ICU', '9876543213'),
('Nurse Anjali', 'Nurse', 'ICU', '9876543214'),
('Nurse Kumar', 'Nurse', 'General', '9876543215'),
('Admin Singh', 'Admin', 'Administration', '9876543216');

-- Insert Patients
INSERT INTO patients (name, age, blood_group, contact, address) VALUES
('Amit Kumar', 45, 'O+', '9123456789', 'Delhi'),
('Sunita Devi', 32, 'B+', '9123456790', 'Mumbai'),
('Rahul Singh', 67, 'A-', '9123456791', 'Bangalore'),
('Priya Nair', 28, 'O+', '9123456792', 'Chennai'),
('Vikram Shah', 55, 'B-', '9123456793', 'Pune'),
('Anita Gupta', 41, 'AB+', '9123456794', 'Kolkata');

-- Insert Active Admissions
INSERT INTO admissions (patient_id, bed_id, doctor_id, expected_discharge, status, notes) VALUES
(1, 3, 2, '2026-05-20', 'Active', 'General checkup'),
(2, 6, 4, '2026-05-18', 'Active', 'Post-surgery monitoring'),
(3, 11, 3, '2026-05-19', 'Active', 'Emergency trauma'),
(4, 15, 1, '2026-05-22', 'Active', 'Private room recovery');

-- Insert Waitlist
INSERT INTO waitlist (patient_id, requested_ward_type, priority_score, status) VALUES
(5, 'ICU', 500, 'Waiting'),
(6, 'General', 200, 'Waiting');

-- Insert Maintenance Log
INSERT INTO bed_maintenance_log (bed_id, reason, status, resolved_by) VALUES
(8, 'Ventilator servicing', 'Ongoing', 4),
(18, 'Deep cleaning', 'Ongoing', 5);
