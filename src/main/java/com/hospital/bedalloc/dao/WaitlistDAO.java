package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Waitlist;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WaitlistDAO {
    public List<Waitlist> getActiveWaitlist() throws SQLException {
        List<Waitlist> waitlist = new ArrayList<>();
        String sql = "SELECT * FROM vw_waitlist_details";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Waitlist w = new Waitlist();
                w.setWaitlistId(rs.getInt("waitlist_id"));
                w.setPatientId(rs.getInt("patient_id"));
                w.setPatientName(rs.getString("patient_name"));
                w.setRequestedWardType(rs.getString("requested_ward_type"));
                w.setPriorityScore(rs.getInt("priority_score"));
                w.setRequestTime(rs.getTimestamp("request_time"));
                w.setStatus(rs.getString("status"));
                waitlist.add(w);
            }
        }
        return waitlist;
    }

    public boolean updatePriority() throws SQLException {
        String sql = "{CALL sp_update_waitlist_priority()}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            return stmt.execute() || stmt.getUpdateCount() >= 0;
        }
    }
    
    public void processWaitlist() throws SQLException {
        String sql = "{CALL sp_process_waitlist(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
        }
    }
}
