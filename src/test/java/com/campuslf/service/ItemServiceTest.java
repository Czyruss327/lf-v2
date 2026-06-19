package com.campuslf.service;

import com.campuslf.models.ItemReport;

import java.time.LocalDate;

public class ItemServiceTest {

    public static void main(String[] args) {

        ItemService service = new ItemService();

        ItemReport item = new ItemReport();

        item.setAdminId(1);
        item.setCategoryId(1);
        item.setItemName("Black Wallet");
        item.setDescription("Contains student ID");
        item.setLocationFound("Library");
        item.setDateReported(LocalDate.now());
        item.setDatePosted(LocalDate.now());
        item.setFinderStudentId("2024-0001");
        item.setFinderContactNum("09123456789");
        item.setImageUrl("wallet.jpg");
        item.setReportStatus("Unclaimed");

        boolean success = service.addItem(item);

        System.out.println("Item added: " + success);
    }
}