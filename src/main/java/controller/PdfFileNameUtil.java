package controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

final class PdfFileNameUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HHmmss");

    private PdfFileNameUtil() {
    }

    static String reportFileName(boolean foundReport, boolean anonymousFinder, int caseId,
                                 String personName, String itemName, LocalDate filingDate) {
        String person = anonymousFinder
                ? "Anon#" + Math.max(0, caseId)
                : camelPart(personName, foundReport ? "Finder" : "Owner");
        String item = camelPart(itemName, "Item");
        String date = datePart(filingDate);
        String time = LocalTime.now().format(TIME_FMT);
        return person + "_" + item + "_" + date + "_" + time + ".pdf";
    }

    static String claimSlipFileName(String claimantName, String itemName, LocalDate releasedDate, LocalTime releaseTime) {
        return camelPart(claimantName, "Claimant")
                + "_" + camelPart(itemName, "Item")
                + "_" + datePart(releasedDate)
                + "_" + timePart(releaseTime)
                + ".pdf";
    }

    static String bulkReportFileName(String status, String groupBy, String category,
                                     LocalDate fromDate, LocalDate toDate, String location) {
        StringBuilder name = new StringBuilder();
        name.append(camelPart(status, "All")).append("_")
                .append(camelPart(groupBy, "Status")).append("_Bulk_Report");
        if (category != null && !category.isBlank() && !"All".equalsIgnoreCase(category)) {
            name.append("_").append(camelPart(category, "Category"));
        }
        if (fromDate != null || toDate != null) {
            name.append("_").append(datePart(fromDate)).append("_To_").append(datePart(toDate));
        }
        if (location != null && !location.isBlank()) {
            name.append("_").append(camelPart(location, "Location"));
        }
        return name + ".pdf";
    }

    private static String camelPart(String value, String fallback) {
        String source = value == null || value.isBlank() ? fallback : value;
        String converted = Arrays.stream(source.trim().split("[^A-Za-z0-9]+"))
                .filter(part -> !part.isBlank())
                .map(PdfFileNameUtil::capitalize)
                .collect(Collectors.joining());
        return converted.isBlank() ? fallback : converted;
    }

    private static String capitalize(String value) {
        if (value.isBlank()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return lower.substring(0, 1).toUpperCase(Locale.ROOT) + lower.substring(1);
    }

    private static String datePart(LocalDate date) {
        return (date == null ? LocalDate.now() : date).format(DATE_FMT);
    }

    private static String timePart(LocalTime time) {
        return (time == null ? LocalTime.now() : time).format(TIME_FMT);
    }
}
