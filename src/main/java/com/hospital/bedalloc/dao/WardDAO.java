package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Ward;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WardDAO {

    // ── READ ─────────────────────────────────────────────────────────────────
    public List<Ward> getAllWards() throws SQLException {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT * FROM wards ORDER BY ward_type";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                wards.add(mapRow(rs));
            }
        }
        return wards;
    }

    public Ward getWardById(int wardId) throws SQLException {
        String sql = "SELECT * FROM wards WHERE ward_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, wardId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public boolean insertWard(Ward ward) throws SQLException {
        String sql = "INSERT INTO wards (ward_type, floor, capacity, daily_charge) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ward.getWardType());
            pstmt.setInt(2, ward.getFloor());
            pstmt.setInt(3, ward.getCapacity());
            pstmt.setDouble(4, ward.getDailyCharge());
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateWard(Ward ward) throws SQLException {
        String sql = "UPDATE wards SET ward_type=?, floor=?, capacity=?, daily_charge=? WHERE ward_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ward.getWardType());
            pstmt.setInt(2, ward.getFloor());
            pstmt.setInt(3, ward.getCapacity());
            pstmt.setDouble(4, ward.getDailyCharge());
            pstmt.setInt(5, ward.getWardId());
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteWard(int wardId) throws SQLException {
        // Block deletion if ward has beds
        String checkSql = "SELECT COUNT(*) FROM beds WHERE ward_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, wardId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Cannot delete a ward that still has beds assigned to it.");
                }
            }
        }
        String sql = "DELETE FROM wards WHERE ward_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, wardId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private Ward mapRow(ResultSet rs) throws SQLException {
        return new Ward(
            rs.getInt("ward_id"),
            rs.getString("ward_type"),
            rs.getInt("floor"),
            rs.getInt("capacity"),
            rs.getDouble("daily_charge")
        );
    }
}
