package com.campuslf.dao;

import com.campuslf.database.DatabaseConnection;
import com.campuslf.models.Claim;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClaimDAO {

    public boolean addClaim(Claim claim) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = prepareInsert(conn, claim)) {
            return executeInsert(pstmt, claim);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Claim> getClaimsByReportId(int reportId) {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM claim WHERE report_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reportId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClaim(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Claim> getAllClaims(String statusFilter) {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM claim";
        if (statusFilter != null && !statusFilter.isBlank()) {
            sql += " WHERE claim_status = ?";
        }
        sql += " ORDER BY date_claimed DESC, claim_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (statusFilter != null && !statusFilter.isBlank()) {
                pstmt.setString(1, statusFilter);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToClaim(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Claim getClaimById(int claimId) {
        String sql = "SELECT * FROM claim WHERE claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, claimId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToClaim(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Claim> getClaimsByLostReportId(int lostReportId) {
        return getClaimsByLinkedReport("lost_report_id", lostReportId);
    }

    public List<Claim> getClaimsByFoundReportId(int foundReportId) {
        return getClaimsByLinkedReport("found_report_id", foundReportId);
    }

    public boolean updateClaimStatus(int claimId, String newStatus) {
        String sql = "UPDATE claim SET claim_status = " + claimStatusParameter() + " WHERE claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, claimId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateVerificationNotes(int claimId, String notes) {
        String sql = "UPDATE claim SET verification_notes = ? WHERE claim_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureClaimColumns(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, notes);
                pstmt.setInt(2, claimId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private PreparedStatement prepareInsert(Connection conn, Claim claim) throws SQLException {
        ensureClaimColumns(conn);
        String sql = "INSERT INTO claim (report_id, lost_report_id, found_report_id, admin_id, claimant_name, claimant_student_id, " +
                "claimant_id, claimant_contact, claimant_contact_number, course_section, claimant_signature, verification_notes, " +
                "claim_status, date_claimed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + claimStatusParameter() + ", ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, claim.getReportId());
        pstmt.setObject(2, claim.getLostReportId());
        pstmt.setObject(3, claim.getFoundReportId());
        pstmt.setInt(4, claim.getAdminId());
        pstmt.setString(5, claim.getClaimantName());
        pstmt.setString(6, valueOrEmpty(claim.getClaimantStudentId()));
        pstmt.setString(7, valueOrEmpty(firstPresent(claim.getClaimantId(), claim.getClaimantStudentId())));
        pstmt.setString(8, valueOrEmpty(claim.getClaimantContact()));
        pstmt.setString(9, valueOrEmpty(firstPresent(claim.getClaimantContactNumber(), claim.getClaimantContact())));
        pstmt.setString(10, valueOrEmpty(claim.getCourseSection()));
        pstmt.setString(11, claim.getClaimantSignature());
        pstmt.setString(12, claim.getVerificationNotes());
        pstmt.setString(13, claim.getClaimStatus());
        pstmt.setTimestamp(14, Timestamp.valueOf(claim.getDateClaimedAt() == null ? LocalDateTime.now() : claim.getDateClaimedAt()));
        return pstmt;
    }

    private boolean executeInsert(PreparedStatement pstmt, Claim claim) throws SQLException {
        int affected = pstmt.executeUpdate();
        if (affected > 0) {
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    claim.setClaimId(keys.getInt(1));
                }
            }
            return true;
        }
        return false;
    }

    private Claim mapResultSetToClaim(ResultSet rs) throws SQLException {
        Claim claim = new Claim();
        claim.setClaimId(rs.getInt("claim_id"));
        claim.setReportId(rs.getInt("report_id"));
        claim.setLostReportId(readOptionalInteger(rs, "lost_report_id"));
        claim.setFoundReportId(readOptionalInteger(rs, "found_report_id"));
        claim.setAdminId(rs.getInt("admin_id"));
        claim.setClaimantName(rs.getString("claimant_name"));
        claim.setClaimantStudentId(rs.getString("claimant_student_id"));
        claim.setClaimantId(readOptionalString(rs, "claimant_id"));
        claim.setClaimantContact(rs.getString("claimant_contact"));
        claim.setClaimantContactNumber(readOptionalString(rs, "claimant_contact_number"));
        claim.setCourseSection(rs.getString("course_section"));
        claim.setClaimantSignature(readOptionalString(rs, "claimant_signature"));
        claim.setVerificationNotes(readOptionalString(rs, "verification_notes"));
        claim.setClaimStatus(rs.getString("claim_status"));
        Timestamp dateClaimed = rs.getTimestamp("date_claimed");
        claim.setDateClaimedAt(dateClaimed == null ? null : dateClaimed.toLocalDateTime());
        return claim;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private List<Claim> getClaimsByLinkedReport(String column, int reportId) {
        List<Claim> list = new ArrayList<>();
        String sql = "SELECT * FROM claim WHERE " + column + " = ? ORDER BY date_claimed DESC, claim_id DESC";
        try (Connection conn = DatabaseConnection.getConnection()) {
            ensureClaimColumns(conn);
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, reportId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToClaim(rs));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private String claimStatusParameter() {
        return DatabaseConnection.isSqlite() ? "?" : "CAST(? AS claim_status_enum)";
    }

    private void ensureClaimColumns(Connection conn) throws SQLException {
        if (!DatabaseConnection.isSqlite()) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE claim ALTER COLUMN date_claimed TYPE TIMESTAMP USING date_claimed::timestamp");
                stmt.execute("ALTER TABLE claim ADD COLUMN IF NOT EXISTS lost_report_id INTEGER");
                stmt.execute("ALTER TABLE claim ADD COLUMN IF NOT EXISTS found_report_id INTEGER");
                stmt.execute("ALTER TABLE claim ADD COLUMN IF NOT EXISTS claimant_id TEXT");
                stmt.execute("ALTER TABLE claim ADD COLUMN IF NOT EXISTS claimant_contact_number TEXT");
                stmt.execute("ALTER TABLE claim ADD COLUMN IF NOT EXISTS claimant_signature TEXT");
                stmt.execute("ALTER TABLE claim ADD COLUMN IF NOT EXISTS verification_notes TEXT");
            }
            return;
        }
        addColumnIfMissing(conn, "claim", "lost_report_id", "INTEGER");
        addColumnIfMissing(conn, "claim", "found_report_id", "INTEGER");
        addColumnIfMissing(conn, "claim", "claimant_id", "TEXT");
        addColumnIfMissing(conn, "claim", "claimant_contact_number", "TEXT");
        addColumnIfMissing(conn, "claim", "claimant_signature", "TEXT");
        addColumnIfMissing(conn, "claim", "verification_notes", "TEXT");
    }

    private void addColumnIfMissing(Connection conn, String table, String column, String definition) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            if (rs.next()) {
                return;
            }
        }
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        }
    }

    private String readOptionalString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null;
        }
    }

    private Integer readOptionalInteger(ResultSet rs, String column) {
        try {
            int value = rs.getInt(column);
            return rs.wasNull() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }

    private String firstPresent(String first, String second) {
        return first == null || first.isBlank() ? second : first;
    }
}
