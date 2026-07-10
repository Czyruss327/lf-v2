package com.campuslf.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime dateClaimedAt;
    private Integer lostReportId;
    private Integer foundReportId;
    private String claimantId;
    private String claimantContactNumber;
    private String claimantSignature;
    private String verificationNotes;

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
    public void setDateClaimed(LocalDate dateClaimed) {
        this.dateClaimed = dateClaimed;
        if (dateClaimed != null && dateClaimedAt == null) {
            this.dateClaimedAt = dateClaimed.atStartOfDay();
        }
    }

    public LocalDateTime getDateClaimedAt() { return dateClaimedAt; }
    public void setDateClaimedAt(LocalDateTime dateClaimedAt) {
        this.dateClaimedAt = dateClaimedAt;
        if (dateClaimedAt != null) {
            this.dateClaimed = dateClaimedAt.toLocalDate();
        }
    }

    public Integer getLostReportId() { return lostReportId; }
    public void setLostReportId(Integer lostReportId) { this.lostReportId = lostReportId; }

    public Integer getFoundReportId() { return foundReportId; }
    public void setFoundReportId(Integer foundReportId) { this.foundReportId = foundReportId; }

    public String getClaimantId() { return claimantId; }
    public void setClaimantId(String claimantId) { this.claimantId = claimantId; }

    public String getClaimantContactNumber() { return claimantContactNumber; }
    public void setClaimantContactNumber(String claimantContactNumber) { this.claimantContactNumber = claimantContactNumber; }

    public String getClaimantSignature() { return claimantSignature; }
    public void setClaimantSignature(String claimantSignature) { this.claimantSignature = claimantSignature; }

    public String getVerificationNotes() { return verificationNotes; }
    public void setVerificationNotes(String verificationNotes) { this.verificationNotes = verificationNotes; }
}
