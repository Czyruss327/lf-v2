package com.campuslf.service;

import com.campuslf.dao.ItemReportDAO;
import com.campuslf.models.ItemReport;
import java.time.LocalDate;
import java.util.List;

public class ItemService {

    private final ItemReportDAO itemDAO;

    public ItemService() {
        this.itemDAO = new ItemReportDAO();
    }

    public boolean addItem(ItemReport report) {

        if (report == null) {
            return false;
        }

        if (report.getItemName() == null ||
                report.getItemName().isBlank()) {
            return false;
        }

        if (report.getLocationFound() == null ||
                report.getLocationFound().isBlank()) {
            return false;
        }

        if (report.getReportStatus() == null) {
            report.setReportStatus("Unclaimed");
        }

        if (report.getDateReported() == null) {
            report.setDateReported(LocalDate.now());
        }

        if (report.getDatePosted() == null) {
            report.setDatePosted(LocalDate.now());
        }

        return itemDAO.addItemReport(report);
    }
    public List<ItemReport> getPendingItems() {
        return itemDAO.getAllItemReports("Unclaimed");
    }

    public List<ItemReport> getClaimedItems() {
        return itemDAO.getAllItemReports("Claimed");
    }

    public List<ItemReport> getVisibleItems(boolean includeClaimed) {
        return includeClaimed
                ? itemDAO.getAllItemReports(null)
                : itemDAO.getAllItemReports("Unclaimed");
    }

    public ItemReport getItemById(int reportId) {
        return itemDAO.getItemReportById(reportId);
    }

    public boolean markClaimed(int reportId) {
        return itemDAO.updateReportStatus(reportId, "Claimed");
    }
}
