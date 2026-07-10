package com.campuslf.dao;

import com.campuslf.database.DatabaseConnection;
import com.campuslf.models.ActivityLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityLogDAO {

    private static final Logger LOGGER = Logger.getLogger(ActivityLogDAO.class.getName());

    public boolean addLog(int adminId, String activity) {
        String sql = "INSERT INTO activity_logs (admin_id, activity) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            pstmt.setString(2, activity);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add activity log", e);
            return false;
        }
    }

    public List<ActivityLog> getAllLogs() {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = """
                SELECT log_id, admin_id, activity, timestamp
                FROM activity_logs
                ORDER BY log_id DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ActivityLog log = new ActivityLog();
                log.setLogId(rs.getInt("log_id"));
                log.setAdminId(rs.getInt("admin_id"));
                log.setActivity(rs.getString("activity"));

                // Null-safe timestamp conversion
                Timestamp ts = rs.getTimestamp("timestamp");
                if (ts != null) {
                    log.setTimestamp(ts.toLocalDateTime());
                } else {
                    log.setTimestamp(null);
                }

                logs.add(log);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load activity logs", e);
        }
        return logs;
    }

    public List<ActivityLog> getLogsByAdminId(int adminId) {
        List<ActivityLog> logs = new ArrayList<>();
        String sql = """
                SELECT log_id, admin_id, activity, timestamp
                FROM activity_logs
                WHERE admin_id = ?
                ORDER BY log_id DESC
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, adminId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    ActivityLog log = new ActivityLog();
                    log.setLogId(rs.getInt("log_id"));
                    log.setAdminId(rs.getInt("admin_id"));
                    log.setActivity(rs.getString("activity"));

                    Timestamp ts = rs.getTimestamp("timestamp");
                    log.setTimestamp(ts == null ? null : ts.toLocalDateTime());

                    logs.add(log);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load activity logs for admin " + adminId, e);
        }
        return logs;
    }
}
