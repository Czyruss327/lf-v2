package com.campuslf.models;

import java.time.LocalDateTime;

public class Admin {
    private int adminId;
    private String username;
    private String password;      // hashed in real app
    private LocalDateTime createdAt;

    // Constructors
    public Admin() {}

    public Admin(int adminId, String username, String password, LocalDateTime createdAt) {
        this.adminId = adminId;
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getAdminId() { return adminId; }
    public void setAdminId(int adminId) { this.adminId = adminId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}