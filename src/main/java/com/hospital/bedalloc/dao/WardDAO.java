package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Ward;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WardDAO {
    public List<Ward> getAllWards() throws SQLException {
        List<Ward> wards = new ArrayList<>();
        String sql = "SELECT * FROM wards";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                wards.add(new Ward(
                    rs.getInt("ward_id"),
                    rs.getString("ward_type"),
                    rs.getInt("floor"),
                    rs.getInt("capacity"),
                    rs.getDouble("daily_charge")
                ));
            }
        }
        return wards;
    }

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
}
