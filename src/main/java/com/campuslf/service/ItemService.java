package com.campuslf.service;

import com.campuslf.dao.ItemReportDAO;
import com.campuslf.models.ItemReport;
import com.campuslf.models.ReportStatus;
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
            report.setReportStatus(ReportStatus.LOST);
        } else {
            report.setReportStatus(ReportStatus.normalize(report.getReportStatus()));
        }

        if (report.getDateReported() == null) {
            report.setDateReported(LocalDate.now());
        }

        if (report.getDatePosted() == null) {
            report.setDatePosted(LocalDate.now());
        }

        boolean added = itemDAO.addItemReport(report);
        if (added) {
            markMatchingReports(report);
        }
        return added;
    }
    public List<ItemReport> getPendingItems() {
        return itemDAO.getAllItemReports(ReportStatus.LOST);
    }

    public List<ItemReport> getClaimedItems() {
        return itemDAO.getAllItemReports(ReportStatus.CLAIMED);
    }

    public List<ItemReport> getVisibleItems(boolean includeClaimed) {
        return includeClaimed
                ? itemDAO.getAllItemReports(null)
                : itemDAO.getAllItemReports(ReportStatus.FOUND);
    }

    public ItemReport getItemById(int reportId) {
        return itemDAO.getItemReportById(reportId);
    }

    public boolean markClaimed(int reportId) {
        return itemDAO.updateReportStatus(reportId, ReportStatus.CLAIMED);
    }

    public boolean markResolved(int reportId) {
        return itemDAO.updateReportStatus(reportId, ReportStatus.RESOLVED);
    }

    private void markMatchingReports(ItemReport report) {
        String status = ReportStatus.normalize(report.getReportStatus());
        if (ReportStatus.LOST.equals(status)) {
            ItemReport foundMatch = itemDAO.findOpenMatch(report, ReportStatus.FOUND);
            if (foundMatch != null) {
                itemDAO.updateReportStatus(report.getReportId(), ReportStatus.RESOLVED);
                itemDAO.updateReportStatus(foundMatch.getReportId(), ReportStatus.CLAIMED);
            }
        } else if (ReportStatus.FOUND.equals(status)) {
            ItemReport lostMatch = itemDAO.findOpenMatch(report, ReportStatus.LOST);
            if (lostMatch != null) {
                itemDAO.updateReportStatus(lostMatch.getReportId(), ReportStatus.RESOLVED);
                itemDAO.updateReportStatus(report.getReportId(), ReportStatus.CLAIMED);
            }
        }
    }
}
