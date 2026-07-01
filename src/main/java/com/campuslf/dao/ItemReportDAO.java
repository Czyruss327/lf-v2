package com.campuslf.dao;

import com.campuslf.database.DatabaseConnection;
import com.campuslf.models.ItemReport;
import com.campuslf.models.ReportStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ItemReportDAO {

    private static final Logger LOGGER = Logger.getLogger(ItemReportDAO.class.getName());

    // INSERT
    public boolean addItemReport(ItemReport report) {
        String sql = "INSERT INTO item_reports (admin_id, category_id, name, description, " +
                "location_found, date_reported, date_posted, finder_student_id, finder_contact_num, " +
                "image_url, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS report_status))";

        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureReportStatusLabels(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setInt(1, report.getAdminId());
                pstmt.setInt(2, report.getCategoryId());
                pstmt.setString(3, report.getItemName());
                pstmt.setString(4, report.getDescription());
                pstmt.setString(5, report.getLocationFound());
                pstmt.setDate(6, Date.valueOf(report.getDateReported()));
                pstmt.setDate(7, Date.valueOf(report.getDatePosted()));
                pstmt.setString(8, valueOrEmpty(report.getFinderStudentId()));
                pstmt.setString(9, valueOrEmpty(report.getFinderContactNum()));
                pstmt.setString(10, report.getImageUrl());
                pstmt.setString(11, ReportStatus.normalize(report.getReportStatus()));

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            report.setReportId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to add item report", e);
            return false;
        }
    }

    // READ all items (optional filter by status)
    public List<ItemReport> getAllItemReports(String statusFilter) {
        List<ItemReport> list = new ArrayList<>();
        String sql = """
                SELECT report_id, admin_id, category_id, name, description, location_found,
                       date_reported, date_posted, finder_student_id, finder_contact_num,
                       image_url, status
                FROM item_reports
                """;
        if (statusFilter != null && !statusFilter.isEmpty()) {
            sql += " WHERE status = CAST(? AS report_status)";
        }
        sql += " ORDER BY date_posted DESC, report_id DESC";

        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureReportStatusLabels(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                if (statusFilter != null && !statusFilter.isEmpty()) {
                    pstmt.setString(1, ReportStatus.normalize(statusFilter));
                }

                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToItemReport(rs));
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load item reports", e);
        }
        return list;
    }

    // READ single item by ID
    public ItemReport getItemReportById(int reportId) {
        String sql = """
                SELECT report_id, admin_id, category_id, name, description, location_found,
                       date_reported, date_posted, finder_student_id, finder_contact_num,
                       image_url, status
                FROM item_reports
                WHERE report_id = ?
                """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToItemReport(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to load item report " + reportId, e);
        }
        return null;
    }

    // UPDATE item status
    public boolean updateReportStatus(int reportId, String newStatus) {
        String sql = "UPDATE item_reports SET status = CAST(? AS report_status) WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureReportStatusLabels(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, ReportStatus.normalize(newStatus));
                pstmt.setInt(2, reportId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to update item report status", e);
            return false;
        }
    }

    public ItemReport findOpenMatch(ItemReport report, String status) {
        String sql = """
                SELECT report_id, admin_id, category_id, name, description, location_found,
                       date_reported, date_posted, finder_student_id, finder_contact_num,
                       image_url, status
                FROM item_reports
                WHERE report_id <> ?
                  AND status = CAST(? AS report_status)
                  AND category_id = ?
                  AND lower(trim(name)) = lower(trim(?))
                ORDER BY date_posted ASC, report_id ASC
                LIMIT 1
                """;
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureReportStatusLabels(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setInt(1, report.getReportId());
                pstmt.setString(2, ReportStatus.normalize(status));
                pstmt.setInt(3, report.getCategoryId());
                pstmt.setString(4, report.getItemName());

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToItemReport(rs);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to find matching item report", e);
        }
        return null;
    }

    // DELETE (or archive) an item report
    public boolean deleteItemReport(int reportId) {
        String sql = "DELETE FROM item_reports WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to delete item report", e);
            return false;
        }
    }

    // Helper to map ResultSet to ItemReport object
    private ItemReport mapResultSetToItemReport(ResultSet rs) throws SQLException {
        ItemReport report = new ItemReport();
        report.setReportId(rs.getInt("report_id"));
        report.setAdminId(rs.getInt("admin_id"));
        report.setCategoryId(rs.getInt("category_id"));
        report.setItemName(rs.getString("name"));
        report.setDescription(rs.getString("description"));
        report.setLocationFound(rs.getString("location_found"));
        Date dateReported = rs.getDate("date_reported");
        Date datePosted = rs.getDate("date_posted");
        report.setDateReported(dateReported == null ? null : dateReported.toLocalDate());
        report.setDatePosted(datePosted == null ? null : datePosted.toLocalDate());
        report.setFinderStudentId(rs.getString("finder_student_id"));
        report.setFinderContactNum(rs.getString("finder_contact_num"));
        report.setImageUrl(rs.getString("image_url"));
        report.setReportStatus(ReportStatus.normalize(rs.getString("status")));
        return report;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private void ensureReportStatusLabels(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TYPE report_status ADD VALUE IF NOT EXISTS 'LOST'");
            stmt.execute("ALTER TYPE report_status ADD VALUE IF NOT EXISTS 'FOUND'");
            stmt.execute("ALTER TYPE report_status ADD VALUE IF NOT EXISTS 'CLAIMED'");
            stmt.execute("ALTER TYPE report_status ADD VALUE IF NOT EXISTS 'RESOLVED'");
        }
    }
}
