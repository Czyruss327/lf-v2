package com.campuslf.service;

import com.campuslf.dao.ItemReportDAO;
import com.campuslf.models.ItemReport;
import com.campuslf.models.ReportStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

public class ItemService {

    private final ItemReportDAO itemDAO;
    private static final Pattern WORD_SPLIT = Pattern.compile("[^a-z0-9]+");
    private static final Set<String> NOISE_WORDS = Set.of(
            "a", "an", "and", "at", "for", "from", "in", "is", "it", "of",
            "on", "or", "the", "to", "with", "lost", "found", "item");

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
        if (report.getDateReportedAt() == null) {
            report.setDateReportedAt(LocalDateTime.now());
        }

        if (report.getDatePosted() == null) {
            report.setDatePosted(LocalDate.now());
        }

        return itemDAO.addItemReport(report);
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

    public List<ItemReport> findPossibleFoundMatches(int lostReportId) {
        ItemReport lostReport = itemDAO.getItemReportById(lostReportId);
        if (lostReport == null || !ReportStatus.LOST.equals(lostReport.getReportStatus())) {
            return List.of();
        }

        return itemDAO.getAllItemReports(ReportStatus.FOUND).stream()
                .map(foundReport -> new Match(foundReport, matchScore(lostReport, foundReport)))
                .filter(match -> match.score() >= 2)
                .sorted(Comparator
                        .comparingInt(Match::score).reversed()
                        .thenComparing(match -> match.report().getDatePosted(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(match -> match.report().getReportId()))
                .map(Match::report)
                .limit(8)
                .collect(Collectors.toList());
    }

    private int matchScore(ItemReport lostReport, ItemReport foundReport) {
        int score = 0;
        if (lostReport.getCategoryId() == foundReport.getCategoryId()) {
            score += 3;
        }

        Set<String> lostNameWords = searchableWords(lostReport.getItemName());
        Set<String> foundNameWords = searchableWords(foundReport.getItemName());
        Set<String> lostDescriptionWords = searchableWords(lostReport.getDescription());
        Set<String> foundDescriptionWords = searchableWords(foundReport.getDescription());

        score += overlapCount(lostNameWords, foundNameWords) * 3;
        score += overlapCount(lostNameWords, foundDescriptionWords) * 2;
        score += overlapCount(lostDescriptionWords, foundNameWords) * 2;
        score += overlapCount(lostDescriptionWords, foundDescriptionWords);

        if (sameText(lostReport.getItemName(), foundReport.getItemName())) {
            score += 5;
        }
        return score;
    }

    private Set<String> searchableWords(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        return WORD_SPLIT.splitAsStream(value.toLowerCase())
                .filter(word -> word.length() > 2)
                .filter(word -> !NOISE_WORDS.contains(word))
                .collect(Collectors.toSet());
    }

    private int overlapCount(Set<String> first, Set<String> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String word : first) {
            if (second.contains(word)) {
                count++;
            }
        }
        return count;
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
    }

    private record Match(ItemReport report, int score) {
    }
}
