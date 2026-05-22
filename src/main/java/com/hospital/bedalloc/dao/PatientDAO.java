package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {

    // ── CREATE ──────────────────────────────────────────────────────────────
    public int insertPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (name, age, blood_group, contact, address, emergency_contact) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getBloodGroup());
            pstmt.setString(4, patient.getContact());
            pstmt.setString(5, patient.getAddress());
            pstmt.setString(6, patient.getEmergencyContact());
            pstmt.executeUpdate();
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        return -1;
    }

    // ── READ ─────────────────────────────────────────────────────────────────
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients ORDER BY registered_date DESC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                patients.add(mapRow(rs));
            }
        }
        return patients;
    }

    public Patient getPatientById(int id) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Patient getPatientByContact(String contact) throws SQLException {
        String sql = "SELECT * FROM patients WHERE contact = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contact);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public List<Patient> searchPatients(String keyword) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients WHERE name LIKE ? OR contact LIKE ? ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String like = "%" + keyword + "%";
            pstmt.setString(1, like);
            pstmt.setString(2, like);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) patients.add(mapRow(rs));
            }
        }
        return patients;
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updatePatient(Patient patient) throws SQLException {
        String sql = "UPDATE patients SET name=?, age=?, blood_group=?, contact=?, address=?, emergency_contact=? WHERE patient_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getName());
            pstmt.setInt(2, patient.getAge());
            pstmt.setString(3, patient.getBloodGroup());
            pstmt.setString(4, patient.getContact());
            pstmt.setString(5, patient.getAddress());
            pstmt.setString(6, patient.getEmergencyContact());
            pstmt.setInt(7, patient.getPatientId());
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deletePatient(int id) throws SQLException {
        // Check for active admissions first
        String checkSql = "SELECT COUNT(*) FROM admissions WHERE patient_id = ? AND status = 'Active'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete patient with an active admission.");
                }
            }
        }
        String sql = "DELETE FROM patients WHERE patient_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private Patient mapRow(ResultSet rs) throws SQLException {
        Patient p = new Patient();
        p.setPatientId(rs.getInt("patient_id"));
        p.setName(rs.getString("name"));
        p.setAge(rs.getInt("age"));
        p.setBloodGroup(rs.getString("blood_group"));
        p.setContact(rs.getString("contact"));
        p.setAddress(rs.getString("address"));
        p.setEmergencyContact(rs.getString("emergency_contact"));
        p.setRegisteredDate(rs.getTimestamp("registered_date"));
        return p;
    }
}
