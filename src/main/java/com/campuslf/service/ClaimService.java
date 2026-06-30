package com.campuslf.service;

import com.campuslf.dao.ClaimDAO;
import com.campuslf.models.Claim;
import com.campuslf.dao.ItemReportDAO;

import java.time.LocalDate;

public class ClaimService {

    private final ClaimDAO claimDAO;

    public ClaimService() {
        this.claimDAO = new ClaimDAO();
    }

    public boolean submitClaim(
            Claim claim
    ) {

        if (claim == null) {
            return false;
        }

        if (claim.getClaimantName() == null
                || claim.getClaimantName().isBlank()) {
            return false;
        }

        if (claim.getReportId() <= 0) {
            return false;
        }

        if (claim.getClaimStatus() == null) {
            claim.setClaimStatus("Pending");
        }

        if (claim.getDateClaimed() == null) {
            claim.setDateClaimed(LocalDate.now());
        }

        return claimDAO.addClaim(claim);
    }

    public boolean approveClaim(int claimId, int reportId, int adminId) {

        // 1. Validate claim exists
        Claim claim = claimDAO.getClaimsByReportId(reportId)
                .stream()
                .filter(c -> c.getClaimId() == claimId)
                .findFirst()
                .orElse(null);

        if (claim == null) {
            System.out.println("Claim not found");
            return false;
        }

        if (!claim.getClaimStatus().equals("Pending")) {
            System.out.println("Claim already processed");
            return false;
        }

        // 2. Update claim
        boolean claimUpdated =
                claimDAO.updateClaimStatus(claimId, "Approved");

        if (!claimUpdated) return false;

        // 3. Update item
        ItemReportDAO itemDAO = new ItemReportDAO();

        boolean itemUpdated =
                itemDAO.updateReportStatus(reportId, "Claimed");

        if (!itemUpdated) return false;

        // 4. Log
        ActivityLogService logService =
                new ActivityLogService();

        logService.logAction(
                adminId,
                "Approved claim ID: " + claimId +
                        " for item ID: " + reportId
        );

        return true;
    }

    public boolean rejectClaim(int claimId, int adminId) {

        boolean updated =
                claimDAO.updateClaimStatus(claimId, "Rejected");

        if (updated) {

            ActivityLogService logService =
                    new ActivityLogService();

            logService.logAction(
                    adminId,
                    "Rejected claim ID: " + claimId
            );
        }

        return updated;
    }
}
