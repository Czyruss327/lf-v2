package com.campuslf.models;

import java.time.LocalDate;

public class Claim {
    private int claimId;
    private int reportId;
    private int adminId;
    private String claimantName;
    private String claimantStudentId;
    private String claimantContact;
    private String courseSection;
    private String claimStatus;   // 'Pending', 'Approved', 'Rejected'
    private LocalDate dateClaimed;

    public Claim() {}

    public Claim(int claimId, int reportId, int adminId, String claimantName,
                 String claimantStudentId, String claimantContact, String courseSection,
                 String claimStatus, LocalDate dateClaimed) {
        this.claimId = claimId;
        this.reportId = reportId;
        this.adminId = adminId;
        this.claimantName = claimantName;
        this.claimantStudentId = claimantStudentId;
        this.claimantContact = claimantContact;
        this.courseSection = courseSection;
        this.claimStatus = claimStatus;
        this.dateClaimed = dateClaimed;
    }

    // Getters and Setters
    public int getClaimId() { return claimId; }
    public void setClaimId(int claimId) { this.claimId = claimId; }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getClaimantName() { return claimantName; }
    public void setClaimantName(String claimantName) { this.claimantName = claimantName; }

    public String getClaimantStudentId() { return claimantStudentId; }
    public void setClaimantStudentId(String claimantStudentId) { this.claimantStudentId = claimantStudentId; }

    public String getClaimantContact() { return claimantContact; }
    public void setClaimantContact(String claimantContact) { this.claimantContact = claimantContact; }

    public String getCourseSection() { return courseSection; }
    public void setCourseSection(String courseSection) { this.courseSection = courseSection; }

    public String getClaimStatus() { return claimStatus; }
    public void setClaimStatus(String claimStatus) { this.claimStatus = claimStatus; }

    public LocalDate getDateClaimed() { return dateClaimed; }
    public void setDateClaimed(LocalDate dateClaimed) { this.dateClaimed = dateClaimed; }
}