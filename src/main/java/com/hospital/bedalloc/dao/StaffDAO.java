package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {

    // ── READ ─────────────────────────────────────────────────────────────────
    public List<Staff> getAllStaff() throws SQLException {
        List<Staff> staff = new ArrayList<>();
        String sql = "SELECT * FROM staff ORDER BY role, name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) staff.add(mapRow(rs));
        }
        return staff;
    }

    public List<Staff> getDoctors() throws SQLException {
        List<Staff> doctors = new ArrayList<>();
        String sql = "SELECT * FROM staff WHERE role = 'Doctor' ORDER BY name";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) doctors.add(mapRow(rs));
        }
        return doctors;
    }

    public Staff getStaffById(int staffId) throws SQLException {
        String sql = "SELECT * FROM staff WHERE staff_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public boolean insertStaff(Staff staff) throws SQLException {
        String sql = "INSERT INTO staff (name, role, department, phone) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getRole());
            pstmt.setString(3, staff.getDepartment());
            pstmt.setString(4, staff.getPhone());
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateStaff(Staff staff) throws SQLException {
        String sql = "UPDATE staff SET name=?, role=?, department=?, phone=? WHERE staff_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, staff.getName());
            pstmt.setString(2, staff.getRole());
            pstmt.setString(3, staff.getDepartment());
            pstmt.setString(4, staff.getPhone());
            pstmt.setInt(5, staff.getStaffId());
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteStaff(int staffId) throws SQLException {
        String sql = "DELETE FROM staff WHERE staff_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private Staff mapRow(ResultSet rs) throws SQLException {
        Staff s = new Staff();
        s.setStaffId(rs.getInt("staff_id"));
        s.setName(rs.getString("name"));
        s.setRole(rs.getString("role"));
        s.setDepartment(rs.getString("department"));
        s.setPhone(rs.getString("phone"));
        return s;
    }
}
