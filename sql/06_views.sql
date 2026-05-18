-- ============================================================
-- 06_views.sql
-- Description: Creates database views.
-- ============================================================

USE hospital_bed_db;

-- VIEW 1: Current Occupancy Dashboard
CREATE VIEW vw_current_occupancy AS
SELECT 
    w.ward_type,
    COUNT(b.bed_id) AS total_beds,
    SUM(CASE WHEN b.current_status = 'Occupied' THEN 1 ELSE 0 END) AS occupied_count,
    SUM(CASE WHEN b.current_status = 'Available' THEN 1 ELSE 0 END) AS available_count,
    SUM(CASE WHEN b.current_status = 'Maintenance' THEN 1 ELSE 0 END) AS maintenance_count,
    ROUND(
        SUM(CASE WHEN b.current_status = 'Occupied' THEN 1 ELSE 0 END) * 100.0 / COUNT(b.bed_id), 
        2
    ) AS occupancy_rate_percent
FROM wards w
LEFT JOIN beds b ON w.ward_id = b.ward_id
GROUP BY w.ward_type;

-- VIEW 2: Active Admissions
CREATE VIEW vw_active_admissions AS
SELECT 
    a.admission_id,
    p.patient_id,
    p.name AS patient_name,
    p.age,
    p.blood_group,
    p.contact,
    w.ward_type,
    w.floor,
    b.bed_id,
    b.bed_number,
    s.name AS doctor_name,
    s.phone AS doctor_phone,
    a.admission_date,
    a.expected_discharge,
    DATEDIFF(COALESCE(a.actual_discharge, NOW()), a.admission_date) AS days_admitted,
    a.notes
FROM admissions a
INNER JOIN patients p ON a.patient_id = p.patient_id
INNER JOIN beds b ON a.bed_id = b.bed_id
INNER JOIN wards w ON b.ward_id = w.ward_id
LEFT JOIN staff s ON a.doctor_id = s.staff_id
WHERE a.status = 'Active';

-- VIEW 3: Available Beds
CREATE VIEW vw_available_beds AS
SELECT 
    b.bed_id,
    b.bed_number,
    w.ward_id,
    w.ward_type,
    w.floor,
    w.daily_charge,
    b.equipment_status,
    w.capacity - (
        SELECT COUNT(*) 
        FROM admissions a2 
        JOIN beds b2 ON a2.bed_id = b2.bed_id 
        JOIN wards w2 ON b2.ward_id = w2.ward_id 
        WHERE w2.ward_id = w.ward_id AND a2.status = 'Active'
    ) AS remaining_capacity_in_ward
FROM beds b
JOIN wards w ON b.ward_id = w.ward_id
WHERE b.current_status = 'Available'
ORDER BY w.ward_type, b.bed_number;

-- VIEW 4: Waitlist Details
CREATE VIEW vw_waitlist_details AS
SELECT 
    wl.waitlist_id,
    p.patient_id,
    p.name AS patient_name,
    p.age,
    p.blood_group,
    p.contact,
    wl.requested_ward_type,
    wl.priority_score,
    wl.request_time,
    TIMESTAMPDIFF(HOUR, wl.request_time, NOW()) AS waiting_hours,
    TIMESTAMPDIFF(MINUTE, wl.request_time, NOW()) % 60 AS waiting_minutes,
    CONCAT(
        TIMESTAMPDIFF(HOUR, wl.request_time, NOW()), 'h ',
        TIMESTAMPDIFF(MINUTE, wl.request_time, NOW()) % 60, 'm'
    ) AS waiting_time_formatted,
    wl.status,
    wl.notes
FROM waitlist wl
INNER JOIN patients p ON wl.patient_id = p.patient_id
WHERE wl.status = 'Waiting'
ORDER BY wl.priority_score DESC, wl.request_time ASC;

-- VIEW 5: Bed Turnover
CREATE VIEW vw_bed_turnover AS
SELECT 
    w.ward_type,
    COUNT(a.admission_id) AS total_discharges,
    ROUND(AVG(DATEDIFF(a.actual_discharge, a.admission_date)), 2) AS avg_stay_days,
    ROUND(MAX(DATEDIFF(a.actual_discharge, a.admission_date)), 2) AS max_stay_days,
    ROUND(MIN(DATEDIFF(a.actual_discharge, a.admission_date)), 2) AS min_stay_days,
    ROUND(
        SUM(DATEDIFF(a.actual_discharge, a.admission_date)) / COUNT(DISTINCT a.bed_id), 
        2
    ) AS avg_turnover_per_bed
FROM admissions a
JOIN beds b ON a.bed_id = b.bed_id
JOIN wards w ON b.ward_id = w.ward_id
WHERE a.status = 'Discharged' 
  AND a.actual_discharge IS NOT NULL
  AND a.admission_date >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY w.ward_type;

-- VIEW 6: Doctor Workload
CREATE VIEW vw_doctor_workload AS
SELECT 
    s.staff_id,
    s.name AS doctor_name,
    s.department,
    s.phone,
    COUNT(a.admission_id) AS current_patients,
    GROUP_CONCAT(DISTINCT w.ward_type) AS assigned_wards
FROM staff s
LEFT JOIN admissions a ON s.staff_id = a.doctor_id AND a.status = 'Active'
LEFT JOIN beds b ON a.bed_id = b.bed_id
LEFT JOIN wards w ON b.ward_id = w.ward_id
WHERE s.role = 'Doctor'
GROUP BY s.staff_id, s.name, s.department, s.phone;
