package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Staff;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StaffDAO {
    public List<Staff> getDoctors() throws SQLException {
        List<Staff> doctors = new ArrayList<>();
        String sql = "SELECT * FROM staff WHERE role = 'Doctor'";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Staff s = new Staff();
                s.setStaffId(rs.getInt("staff_id"));
                s.setName(rs.getString("name"));
                s.setRole(rs.getString("role"));
                s.setDepartment(rs.getString("department"));
                s.setPhone(rs.getString("phone"));
                doctors.add(s);
            }
        }
        return doctors;
    }
}
