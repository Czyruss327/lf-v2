package com.campuslf.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LostItemReport {
    private int lostReportId;
    private Integer adminId;
    private int categoryId;
    private String itemName;
    private String description;
    private String imageUrl;
    private String complainantName;
    private String complainantId;
    private String complainantContactNum;
    private boolean anonymous;
    private String locationFound;
    private LocalDate dateFound;
    private LocalDateTime dateReported;
    private String status;

    public int getLostReportId() { return lostReportId; }
    public void setLostReportId(int lostReportId) { this.lostReportId = lostReportId; }

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

    public String getComplainantName() { return complainantName; }
    public void setComplainantName(String complainantName) { this.complainantName = complainantName; }

    public String getComplainantId() { return complainantId; }
    public void setComplainantId(String complainantId) { this.complainantId = complainantId; }

    public String getComplainantContactNum() { return complainantContactNum; }
    public void setComplainantContactNum(String complainantContactNum) { this.complainantContactNum = complainantContactNum; }

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
