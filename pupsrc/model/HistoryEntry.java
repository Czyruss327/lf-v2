package model;

import java.time.LocalDateTime;

/**
 * A single entry in an admin's activity history
 * (e.g. "Login Activity", "Change Password").
 */
public class HistoryEntry {

    private final String action;
    private final LocalDateTime timestamp;

    public HistoryEntry(String action, LocalDateTime timestamp) {
        this.action = action;
        this.timestamp = timestamp;
    }

    public String getAction() {
        return action;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
