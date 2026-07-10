package com.campuslf.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class FoundItemReport {
    private int foundReportId;
    private Integer adminId;
    private int categoryId;
    private String itemName;
    private String description;
    private String imageUrl;
    private String finderName;
    private String finderId;
    private String finderContactNumber;
    private boolean anonymous;
    private String locationFound;
    private LocalDate dateFound;
    private LocalDateTime dateReported;
    private String status;

    public int getFoundReportId() { return foundReportId; }
    public void setFoundReportId(int foundReportId) { this.foundReportId = foundReportId; }

    public Integer getAdminId() { return adminId; }
    public void setAdminId(Integer adminId) { this.adminId = adminId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getFinderName() { return finderName; }
    public void setFinderName(String finderName) { this.finderName = finderName; }

    public String getFinderId() { return finderId; }
    public void setFinderId(String finderId) { this.finderId = finderId; }

    public String getFinderContactNumber() { return finderContactNumber; }
    public void setFinderContactNumber(String finderContactNumber) { this.finderContactNumber = finderContactNumber; }

    public boolean isAnonymous() { return anonymous; }
    public void setAnonymous(boolean anonymous) { this.anonymous = anonymous; }

    public String getLocationFound() { return locationFound; }
    public void setLocationFound(String locationFound) { this.locationFound = locationFound; }

    public LocalDate getDateFound() { return dateFound; }
    public void setDateFound(LocalDate dateFound) { this.dateFound = dateFound; }

    public LocalDateTime getDateReported() { return dateReported; }
    public void setDateReported(LocalDateTime dateReported) { this.dateReported = dateReported; }

    public String getStatus() { return ReportStatus.normalize(status); }
    public void setStatus(String status) { this.status = ReportStatus.normalize(status); }
}
