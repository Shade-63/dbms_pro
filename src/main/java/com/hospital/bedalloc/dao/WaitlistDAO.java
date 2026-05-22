package com.hospital.bedalloc.dao;

import com.hospital.bedalloc.db.DBConnection;
import com.hospital.bedalloc.model.Waitlist;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WaitlistDAO {

    // ── READ ─────────────────────────────────────────────────────────────────
    public List<Waitlist> getActiveWaitlist() throws SQLException {
        List<Waitlist> waitlist = new ArrayList<>();
        String sql = "SELECT * FROM vw_waitlist_details ORDER BY priority_score DESC, request_time ASC";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                waitlist.add(mapRow(rs));
            }
        }
        return waitlist;
    }

    // ── CREATE ───────────────────────────────────────────────────────────────
    public boolean addToWaitlist(int patientId, String wardType, int priority, String notes) throws SQLException {
        String sql = "INSERT INTO waitlist (patient_id, requested_ward_type, priority_score, notes, status) VALUES (?, ?, ?, ?, 'Waiting')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, wardType);
            pstmt.setInt(3, priority);
            pstmt.setString(4, notes);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── UPDATE — Manual priority change ──────────────────────────────────────
    public boolean updateWaitlistPriority(int waitlistId, int newPriority) throws SQLException {
        String sql = "UPDATE waitlist SET priority_score=? WHERE waitlist_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newPriority);
            pstmt.setInt(2, waitlistId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updatePriority() throws SQLException {
        String sql = "{CALL sp_update_waitlist_priority()}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            return stmt.execute() || stmt.getUpdateCount() >= 0;
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────
    public boolean removeFromWaitlist(int waitlistId) throws SQLException {
        String sql = "DELETE FROM waitlist WHERE waitlist_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, waitlistId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // ── PROCESS (stored procedure) ───────────────────────────────────────────
    public void processWaitlist() throws SQLException {
        String sql = "{CALL sp_process_waitlist(?, ?)}";
        try (Connection conn = DBConnection.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.registerOutParameter(2, Types.INTEGER);
            stmt.execute();
        }
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private Waitlist mapRow(ResultSet rs) throws SQLException {
        Waitlist w = new Waitlist();
        w.setWaitlistId(rs.getInt("waitlist_id"));
        w.setPatientId(rs.getInt("patient_id"));
        w.setPatientName(rs.getString("patient_name"));
        w.setRequestedWardType(rs.getString("requested_ward_type"));
        w.setPriorityScore(rs.getInt("priority_score"));
        w.setRequestTime(rs.getTimestamp("request_time"));
        w.setStatus(rs.getString("status"));
        return w;
    }
}
