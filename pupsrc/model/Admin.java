package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an admin user of the PUPSRC Lost and Found system.
 */
public class Admin {

    private String username;
    private String hashedPassword; // Store a hash in production; plain for now
    private final List<HistoryEntry> history = new ArrayList<>();

    public Admin(String username, String password) {
        this.username = username;
        this.hashedPassword = hashPassword(password);
    }

    // ─── Auth ────────────────────────────────────────────────────────────────

    public boolean checkPassword(String raw) {
        return hashedPassword.equals(hashPassword(raw));
    }

    public void setPassword(String newPassword) {
        this.hashedPassword = hashPassword(newPassword);
    }

    /**
     * Minimal hash for academic use.
     * Replace with BCrypt or similar in a real deployment.
     */
    private String hashPassword(String raw) {
        return Integer.toHexString(raw.hashCode());
    }

    // ─── History ─────────────────────────────────────────────────────────────

    public void addHistory(HistoryEntry entry) {
        history.add(0, entry); // newest first
    }

    public List<HistoryEntry> getHistory() {
        return history;
    }

    // ─── Getters / Setters ───────────────────────────────────────────────────

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Admin{username='" + username + "'}";
    }
}
