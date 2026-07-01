package com.campuslf.dao;

import com.campuslf.database.DatabaseConnection;
import com.campuslf.models.Claim;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

    public boolean updateClaimStatus(int claimId, String newStatus) {
        String sql = "UPDATE claim SET claim_status = CAST(? AS claim_status_enum) WHERE claim_id = ?";
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

    private PreparedStatement prepareInsert(Connection conn, Claim claim) throws SQLException {
        String sql = "INSERT INTO claim (report_id, admin_id, claimant_name, claimant_student_id, " +
                "claimant_contact, course_section, claim_status, date_claimed) " +
                "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS claim_status_enum), ?)";
        PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        pstmt.setInt(1, claim.getReportId());
        pstmt.setInt(2, claim.getAdminId());
        pstmt.setString(3, claim.getClaimantName());
        pstmt.setString(4, valueOrEmpty(claim.getClaimantStudentId()));
        pstmt.setString(5, valueOrEmpty(claim.getClaimantContact()));
        pstmt.setString(6, valueOrEmpty(claim.getCourseSection()));
        pstmt.setString(7, claim.getClaimStatus());
        pstmt.setDate(8, Date.valueOf(claim.getDateClaimed()));
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
        claim.setAdminId(rs.getInt("admin_id"));
        claim.setClaimantName(rs.getString("claimant_name"));
        claim.setClaimantStudentId(rs.getString("claimant_student_id"));
        claim.setClaimantContact(rs.getString("claimant_contact"));
        claim.setCourseSection(rs.getString("course_section"));
        claim.setClaimStatus(rs.getString("claim_status"));
        Date dateClaimed = rs.getDate("date_claimed");
        claim.setDateClaimed(dateClaimed == null ? null : dateClaimed.toLocalDate());
        return claim;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
