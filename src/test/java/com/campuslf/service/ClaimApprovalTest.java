package com.campuslf.service;

public class ClaimApprovalTest {
    public static void main(String[] args) {

        ClaimService service =
                new ClaimService();

        boolean result =
                service.approveClaim(
                        1,  // claim_id (must exist)
                        4,  // report_id (must exist)
                        1   // admin_id
                );

        System.out.println("Approved: " + result);
    }
}