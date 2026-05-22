package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Bed;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BedDAO {

    // ── READ ─────────────────────────────────────────────────────────────────
    public List<Bed> getAllBedsWithWard() throws SQLException {
        List<Bed> beds = new ArrayList<>();
        String sql = "SELECT b.*, w.ward_type FROM beds b JOIN wards w ON b.ward_id = w.ward_id ORDER BY b.ward_id, b.bed_number";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bed bed = mapRow(rs);
                bed.setWardType(rs.getString("ward_type"));
                beds.add(bed);
            }
        }
        return beds;
    }

    public Bed getBedById(int bedId) throws SQLException {
        String sql = "SELECT b.*, w.ward_type FROM beds b JOIN wards w ON b.ward_id = w.ward_id WHERE b.bed_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bedId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Bed bed = mapRow(rs);
                    bed.setWardType(rs.getString("ward_type"));
                    return bed;
                }
            }
        }
        return null;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public boolean insertBed(Bed bed) throws SQLException {
        String sql = "INSERT INTO beds (ward_id, bed_number, equipment_status, current_status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bed.getWardId());
            pstmt.setString(2, bed.getBedNumber());
            pstmt.setString(3, bed.getEquipmentStatus() != null ? bed.getEquipmentStatus() : "Standard");
            pstmt.setString(4, bed.getCurrentStatus() != null ? bed.getCurrentStatus() : "Available");
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────
    public boolean updateBed(Bed bed) throws SQLException {
        String sql = "UPDATE beds SET ward_id=?, bed_number=?, equipment_status=? WHERE bed_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bed.getWardId());
            pstmt.setString(2, bed.getBedNumber());
            pstmt.setString(3, bed.getEquipmentStatus());
            pstmt.setInt(4, bed.getBedId());
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateBedStatus(int bedId, String status) throws SQLException {
        String sql = "UPDATE beds SET current_status = ? WHERE bed_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, bedId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean deleteBed(int bedId) throws SQLException {
        // Block deletion if bed is currently occupied
        String checkSql = "SELECT current_status FROM beds WHERE bed_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            checkStmt.setInt(1, bedId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && "Occupied".equals(rs.getString("current_status"))) {
                    throw new SQLException("Cannot delete an occupied bed. Please discharge the patient first.");
                }
            }
        }
        String sql = "DELETE FROM beds WHERE bed_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, bedId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private Bed mapRow(ResultSet rs) throws SQLException {
        return new Bed(
            rs.getInt("bed_id"),
            rs.getInt("ward_id"),
            rs.getString("bed_number"),
            rs.getString("equipment_status"),
            rs.getString("current_status")
        );
    }
}
