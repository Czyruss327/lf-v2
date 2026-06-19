package com.campuslf.database;

import java.sql.Connection;
import java.sql.SQLException;

public class TestConnection {
    public static void main(String[] args) {
        System.out.println("Attempting to connect to the database...");
        try (Connection conn = DatabaseConnection.getConnection()) {
            System.out.println("✅ Connection successful! The password is correct.");
        } catch (SQLException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}