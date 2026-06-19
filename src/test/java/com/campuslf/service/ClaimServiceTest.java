package com.campuslf.service;

import com.campuslf.models.Claim;

import java.time.LocalDate;

public class ClaimServiceTest {

    public static void main(String[] args) {

        ClaimService service =
                new ClaimService();

        Claim claim = new Claim();

        claim.setReportId(10);
        claim.setAdminId(1);

        claim.setClaimantName(
                "Juan Dela Cruz"
        );

        claim.setClaimantStudentId(
                "2024-10001"
        );

        claim.setClaimantContact(
                "09123456789"
        );

        claim.setCourseSection(
                "BSIT 3-1"
        );

        claim.setClaimStatus(
                "Pending"
        );

        claim.setDateClaimed(
                LocalDate.now()
        );

        boolean success =
                service.submitClaim(claim);

        System.out.println(
                "Claim submitted: "
                        + success
        );
    }
}