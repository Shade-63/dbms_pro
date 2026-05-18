package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO {
    
    public List<Map<String, Object>> getOccupancyReport() throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String sql = "SELECT * FROM vw_current_occupancy";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ward_type", rs.getString("ward_type"));
                row.put("total_beds", rs.getInt("total_beds"));
                row.put("occupied_count", rs.getInt("occupied_count"));
                row.put("available_count", rs.getInt("available_count"));
                row.put("maintenance_count", rs.getInt("maintenance_count"));
                row.put("occupancy_rate", rs.getDouble("occupancy_rate_percent"));
                report.add(row);
            }
        }
        return report;
    }

    public List<Map<String, Object>> getTurnoverReport() throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String sql = "SELECT * FROM vw_bed_turnover";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ward_type", rs.getString("ward_type"));
                row.put("total_discharges", rs.getInt("total_discharges"));
                row.put("avg_stay_days", rs.getDouble("avg_stay_days"));
                row.put("max_stay_days", rs.getDouble("max_stay_days"));
                row.put("min_stay_days", rs.getDouble("min_stay_days"));
                report.add(row);
            }
        }
        return report;
    }

    public List<Map<String, Object>> getDoctorWorkload() throws SQLException {
        List<Map<String, Object>> report = new ArrayList<>();
        String sql = "SELECT * FROM vw_doctor_workload";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("doctor_name", rs.getString("doctor_name"));
                row.put("department", rs.getString("department"));
                row.put("current_patients", rs.getInt("current_patients"));
                row.put("assigned_wards", rs.getString("assigned_wards"));
                report.add(row);
            }
        }
        return report;
    }
}
