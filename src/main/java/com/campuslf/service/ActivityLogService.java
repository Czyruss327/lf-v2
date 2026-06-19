package com.campuslf.service;

import com.campuslf.dao.ActivityLogDAO;
import com.campuslf.models.ActivityLog;

import java.util.List;

public class ActivityLogService {

    private final ActivityLogDAO activityLogDAO;

    public ActivityLogService() {
        this.activityLogDAO = new ActivityLogDAO();
    }

    public boolean logAction(int adminId, String activity) {
        return activityLogDAO.addLog(adminId, activity);
    }

    public List<ActivityLog> getAllLogs() {
        return activityLogDAO.getAllLogs();
    }
}