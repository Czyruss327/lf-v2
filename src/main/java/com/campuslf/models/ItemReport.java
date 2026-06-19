package com.campuslf.models;

import java.time.LocalDate;

public class ItemReport {
    private int reportId;
    private int adminId;
    private int categoryId;          // Foreign key to Category
    private String itemName;
    private String description;
    private String locationFound;
    private LocalDate dateReported;
    private LocalDate datePosted;
    private String finderStudentId;
    private String finderContactNum;
    private String imageUrl;
    private String reportStatus;      // 'Pending', 'Claimed', 'Archived'

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
        this.reportStatus = reportStatus;
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
    public void setDateReported(LocalDate dateReported) { this.dateReported = dateReported; }

    public LocalDate getDatePosted() { return datePosted; }
    public void setDatePosted(LocalDate datePosted) { this.datePosted = datePosted; }

    public String getFinderStudentId() { return finderStudentId; }
    public void setFinderStudentId(String finderStudentId) { this.finderStudentId = finderStudentId; }

    public String getFinderContactNum() { return finderContactNum; }
    public void setFinderContactNum(String finderContactNum) { this.finderContactNum = finderContactNum; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getReportStatus() { return reportStatus; }
    public void setReportStatus(String reportStatus) { this.reportStatus = reportStatus; }
}