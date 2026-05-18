package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Patient;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO {
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

    public Patient getPatientByContact(String contact) throws SQLException {
        String sql = "SELECT * FROM patients WHERE contact = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, contact);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
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
        }
        return null;
    }
}
