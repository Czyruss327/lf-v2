package com.campuslf.models;

import java.time.LocalDateTime;

public class ActivityLog {
    private int logId;
    private int adminId;
    private String activity;
    private LocalDateTime timestamp;

    public ActivityLog() {}

    public ActivityLog(int logId, int adminId, String activity, LocalDateTime timestamp) {
        this.logId = logId;
        this.adminId = adminId;
        this.activity = activity;
        this.timestamp = timestamp;
    }

    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getActivity() { return activity; }
    public void setActivity(String activity) { this.activity = activity; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}