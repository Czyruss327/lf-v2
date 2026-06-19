package com.campuslf.test;

import com.campuslf.dao.*;
import com.campuslf.models.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseTest {
    public static void main(String[] args) {
        // 1. Admin: check if exists before inserting
        AdminDAO adminDAO = new AdminDAO();
        Admin existingAdmin = adminDAO.getAdminByUsername("test_admin");
        if (existingAdmin == null) {
            Admin newAdmin = new Admin();
            newAdmin.setUsername("test_admin");
            newAdmin.setPassword("password123");
            boolean adminAdded = adminDAO.addAdmin(newAdmin);
            System.out.println("Admin added: " + adminAdded);
        } else {
            System.out.println("Admin already exists, skipping insert.");
        }
        Admin fetched = adminDAO.getAdminByUsername("test_admin");
        System.out.println("Fetched admin: " + (fetched != null ? fetched.getUsername() : "null"));
        int adminId = (fetched != null) ? fetched.getAdminId() : 1;

        // 2. Ensure category with id=1 exists (run SQL in Supabase first)
        // For now, let's fetch category ID or assume 1 exists.
        // You can also query category table to get first category ID.
        // Let's just try to get category ID 1; if fails, we'll need to insert.
        // To avoid foreign key error, we'll use a helper to get or create category.
        // But for simplicity, assume you inserted categories with IDs 1-5.

        // 3. Test ItemReport insertion
        ItemReportDAO itemDAO = new ItemReportDAO();
        ItemReport item = new ItemReport();
        item.setAdminId(adminId);
        item.setCategoryId(1); // Ensure category 1 exists
        item.setItemName("Lost Laptop");
        item.setDescription("Dell XPS 13, silver color");
        item.setLocationFound("Library 2nd floor");
        item.setDateReported(LocalDate.now());
        item.setDatePosted(LocalDate.now());
        item.setFinderStudentId("2024-12345");
        item.setFinderContactNum("09123456789");
        item.setImageUrl("https://example.com/laptop.jpg");
        item.setReportStatus("Unclaimed");

        boolean itemAdded = itemDAO.addItemReport(item);
        System.out.println("Item report added: " + itemAdded);
        if (itemAdded) {
            System.out.println("Generated report_id: " + item.getReportId());
        }

        // 4. Retrieve and display unclaimed items
        List<ItemReport> unclaimed = itemDAO.getAllItemReports("Unclaimed");
        System.out.println("Unclaimed items count: " + unclaimed.size());
        for (ItemReport i : unclaimed) {
            System.out.println(i.getItemName() + " - " + i.getReportStatus());
        }

        // 5. Test Claim insertion
        if (itemAdded) {
            ClaimDAO claimDAO = new ClaimDAO();
            Claim claim = new Claim();
            claim.setReportId(item.getReportId());
            claim.setAdminId(adminId);
            claim.setClaimantName("Maria Santos");
            claim.setClaimantStudentId("2023-67890");
            claim.setClaimantContact("09987654321");
            claim.setCourseSection("BSIT 3-2");
            claim.setClaimStatus("Unclaimed");
            claim.setDateClaimed(LocalDate.now());

            boolean claimAdded = claimDAO.addClaim(claim);
            System.out.println("Claim added: " + claimAdded);
        }

        // 6. Test Activity Log
        ActivityLogDAO logDAO = new ActivityLogDAO();
        boolean logAdded = logDAO.addLog(adminId, "Tested database integration");
        System.out.println("Activity log added: " + logAdded);

        List<ActivityLog> logs = logDAO.getAllLogs();
        System.out.println("Total logs: " + logs.size());
    }
}