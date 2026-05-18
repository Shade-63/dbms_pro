package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Bed;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BedDAO {
    public List<Bed> getAllBedsWithWard() throws SQLException {
        List<Bed> beds = new ArrayList<>();
        String sql = "SELECT b.*, w.ward_type FROM beds b JOIN wards w ON b.ward_id = w.ward_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Bed bed = new Bed(
                    rs.getInt("bed_id"),
                    rs.getInt("ward_id"),
                    rs.getString("bed_number"),
                    rs.getString("equipment_status"),
                    rs.getString("current_status")
                );
                bed.setWardType(rs.getString("ward_type"));
                beds.add(bed);
            }
        }
        return beds;
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
}
