package com.campuslf.dao;

import com.campuslf.database.DatabaseConnection;
import com.campuslf.models.Claim;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClaimDAO {

    // INSERT claim – cast claim_status to enum
    public boolean addClaim(Claim claim) {
        String sql = "INSERT INTO claim (report_id, admin_id, claimant_name, claimant_student_id, " +
                "claimant_contact, course_section, claim_status, date_claimed) " +
                "VALUES (?, ?, ?, ?, ?, ?, CAST(? AS claim_status_enum), ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, claim.getReportId());
            pstmt.setInt(2, claim.getAdminId());
            pstmt.setString(3, claim.getClaimantName());
            pstmt.setString(4, claim.getClaimantStudentId());
            pstmt.setString(5, claim.getClaimantContact());
            pstmt.setString(6, claim.getCourseSection());
            pstmt.setString(7, claim.getClaimStatus());   // string -> enum via CAST
            pstmt.setDate(8, Date.valueOf(claim.getDateClaimed()));

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
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // READ claims by report_id
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

    // UPDATE claim status – cast to enum
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

    // Helper to map ResultSet to Claim object
    private Claim mapResultSetToClaim(ResultSet rs) throws SQLException {
        Claim claim = new Claim();
        claim.setClaimId(rs.getInt("claim_id"));
        claim.setReportId(rs.getInt("report_id"));
        claim.setAdminId(rs.getInt("admin_id"));
        claim.setClaimantName(rs.getString("claimant_name"));
        claim.setClaimantStudentId(rs.getString("claimant_student_id"));
        claim.setClaimantContact(rs.getString("claimant_contact"));
        claim.setCourseSection(rs.getString("course_section"));
        claim.setClaimStatus(rs.getString("claim_status")); // enum -> string automatically
        claim.setDateClaimed(rs.getDate("date_claimed").toLocalDate());
        return claim;
    }
}