package com.campuslf.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ItemReport {
    private int reportId;
    private int adminId;
    private int categoryId;          // Foreign key to Category
    private String itemName;
    private String description;
    private String locationFound;
    private LocalDate dateReported;
    private LocalDateTime dateReportedAt;
    private LocalDate datePosted;
    private String finderStudentId;
    private String finderContactNum;
    private String imageUrl;
    private String reportStatus;      // LOST, FOUND, CLAIMED, RESOLVED
    private String reporterName;
    private String reporterId;
    private String reporterContactNum;
    private boolean anonymous;
    private String reportType;

    // Constructors
    public ItemReport() {}

    // All-args constructor (optional)
    public ItemReport(int reportId, int adminId, int categoryId, String itemName,
                      String description, String locationFound, LocalDate dateReported,
                      LocalDate datePosted, String finderStudentId, String finderContactNum,
                      String imageUrl, String reportStatus) {
        this.reportId = reportId;
        this.adminId = adminId;
        this.categoryId = categoryId;
        this.itemName = itemName;
        this.description = description;
        this.locationFound = locationFound;
        this.dateReported = dateReported;
        this.datePosted = datePosted;
        this.finderStudentId = finderStudentId;
        this.finderContactNum = finderContactNum;
        this.imageUrl = imageUrl;
        setReportStatus(reportStatus);
    }

    // Getters and Setters
    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocationFound() { return locationFound; }
    public void setLocationFound(String locationFound) { this.locationFound = locationFound; }

    public LocalDate getDateReported() { return dateReported; }
    public void setDateReported(LocalDate dateReported) {
        this.dateReported = dateReported;
        if (dateReported != null && dateReportedAt == null) {
            this.dateReportedAt = dateReported.atStartOfDay();
        }
    }

    public LocalDateTime getDateReportedAt() { return dateReportedAt; }
    public void setDateReportedAt(LocalDateTime dateReportedAt) {
        this.dateReportedAt = dateReportedAt;
        if (dateReportedAt != null) {
            this.dateReported = dateReportedAt.toLocalDate();
        }
    }

    public LocalDate getDatePosted() { return datePosted; }
    public void setDatePosted(LocalDate datePosted) { this.datePosted = datePosted; }

    public String getFinderStudentId() { return finderStudentId; }
    public void setFinderStudentId(String finderStudentId) { this.finderStudentId = finderStudentId; }

    public String getFinderContactNum() { return finderContactNum; }
    public void setFinderContactNum(String finderContactNum) { this.finderContactNum = finderContactNum; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getReportStatus() { return ReportStatus.normalize(reportStatus); }
    public void setReportStatus(String reportStatus) { this.reportStatus = ReportStatus.normalize(reportStatus); }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getReporterId() { return reporterId; }
    public void setReporterId(String reporterId) { this.reporterId = reporterId; }

    public String getReporterContactNum() { return reporterContactNum; }
    public void setReporterContactNum(String reporterContactNum) { this.reporterContactNum = reporterContactNum; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
}
