package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Admission;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdmissionDAO {

    public static class AllocationResult {
        private int bedId;
        private int admissionId;
        private boolean success;
        private String message;

        public int getBedId() { return bedId; }
        public void setBedId(int bedId) { this.bedId = bedId; }
        public int getAdmissionId() { return admissionId; }
        public void setAdmissionId(int admissionId) { this.admissionId = admissionId; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // ── ALLOCATE (via stored procedure) ─────────────────────────────────────
    public AllocationResult allocateBed(int patientId, String wardType, Integer doctorId, Date expectedDischarge, String notes) throws SQLException {
        AllocationResult result = new AllocationResult();
        String sql = "{CALL sp_allocate_bed(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, patientId);
            stmt.setString(2, wardType);
            if (doctorId != null) stmt.setInt(3, doctorId); else stmt.setNull(3, Types.INTEGER);
            stmt.setDate(4, expectedDischarge);
            stmt.setString(5, notes);

            stmt.registerOutParameter(6, Types.INTEGER);  // p_bed_id
            stmt.registerOutParameter(7, Types.INTEGER);  // p_admission_id
            stmt.registerOutParameter(8, Types.BOOLEAN);  // p_success
            stmt.registerOutParameter(9, Types.VARCHAR);  // p_message

            stmt.execute();

            result.setBedId(stmt.getInt(6));
            result.setAdmissionId(stmt.getInt(7));
            result.setSuccess(stmt.getBoolean(8));
            result.setMessage(stmt.getString(9));
        }
        return result;
    }

    // ── DISCHARGE (via stored procedure) ────────────────────────────────────
    public String dischargePatient(int admissionId, boolean maintenanceNeeded, String maintenanceReason) throws SQLException {
        String sql = "{CALL sp_discharge_patient(?, ?, ?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setInt(1, admissionId);
            stmt.setBoolean(2, maintenanceNeeded);
            stmt.setString(3, maintenanceReason);
            stmt.registerOutParameter(4, Types.VARCHAR); // p_message

            stmt.execute();
            return stmt.getString(4);
        }
    }

    // ── READ — Active Admissions ─────────────────────────────────────────────
    public List<Admission> getActiveAdmissions() throws SQLException {
        List<Admission> admissions = new ArrayList<>();
        String sql = "SELECT * FROM vw_active_admissions";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                admissions.add(mapActiveRow(rs));
            }
        }
        return admissions;
    }

    // ── READ — Admission History (discharged) ─────────────────────────────────
    public List<Admission> getAdmissionHistory() throws SQLException {
        List<Admission> history = new ArrayList<>();
        String sql = "SELECT a.admission_id, a.patient_id, p.name AS patient_name, " +
                     "a.bed_id, b.bed_number, s.name AS doctor_name, " +
                     "a.admission_date, a.expected_discharge, a.actual_discharge, a.status, a.notes " +
                     "FROM admissions a " +
                     "JOIN patients p ON a.patient_id = p.patient_id " +
                     "JOIN beds b ON a.bed_id = b.bed_id " +
                     "LEFT JOIN staff s ON a.doctor_id = s.staff_id " +
                     "WHERE a.status = 'Discharged' " +
                     "ORDER BY a.actual_discharge DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Admission a = mapActiveRow(rs);
                a.setActualDischarge(rs.getTimestamp("actual_discharge"));
                a.setStatus(rs.getString("status"));
                history.add(a);
            }
        }
        return history;
    }

    // ── UPDATE — Edit active admission notes / expected discharge ────────────
    public boolean updateAdmission(int admissionId, Date expectedDischarge, String notes) throws SQLException {
        String sql = "UPDATE admissions SET expected_discharge=?, notes=? WHERE admission_id=? AND status='Active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, expectedDischarge);
            pstmt.setString(2, notes);
            pstmt.setInt(3, admissionId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private Admission mapActiveRow(ResultSet rs) throws SQLException {
        Admission a = new Admission();
        a.setAdmissionId(rs.getInt("admission_id"));
        a.setPatientId(rs.getInt("patient_id"));
        a.setPatientName(rs.getString("patient_name"));
        a.setBedId(rs.getInt("bed_id"));
        a.setBedNumber(rs.getString("bed_number"));
        a.setDoctorName(rs.getString("doctor_name"));
        a.setAdmissionDate(rs.getTimestamp("admission_date"));
        a.setExpectedDischarge(rs.getDate("expected_discharge"));
        try { a.setNotes(rs.getString("notes")); } catch (Exception ignored) {}
        a.setStatus("Active");
        return a;
    }
}
