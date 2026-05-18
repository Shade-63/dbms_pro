-- ============================================================
-- 03_indexes.sql
-- Description: Creates performance indexes.
-- ============================================================

USE hospital_bed_db;

-- Bed status lookups (frequent in allocation queries)
CREATE INDEX idx_bed_status ON beds(current_status);

-- Combined index for ward + status (dashboard filtering)
CREATE INDEX idx_bed_ward_status ON beds(ward_id, current_status);

-- Admission status filtering (discharge panel, active list)
CREATE INDEX idx_adm_status ON admissions(status);

-- Active admission per bed (constraint enforcement)
CREATE INDEX idx_adm_bed_active ON admissions(bed_id, status);

-- Patient name search
CREATE INDEX idx_patient_name ON patients(name);

-- Waitlist sorting by priority and time
CREATE INDEX idx_waitlist_priority ON waitlist(priority_score DESC, request_time ASC);

-- Waitlist status filtering
CREATE INDEX idx_waitlist_status ON waitlist(status, requested_ward_type);

-- Audit log lookups by bed
CREATE INDEX idx_audit_bed ON bed_status_audit(bed_id, changed_at DESC);

-- Maintenance log by bed
CREATE INDEX idx_maint_bed ON bed_maintenance_log(bed_id, start_time DESC);
