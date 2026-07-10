package mapper;

import com.campuslf.models.ReportStatus;
import com.campuslf.models.ItemReport;
import model.Item;

public class ItemMapper {
    public static Item toItem(ItemReport report) {

        Item item = new Item();

        item.setId(report.getReportId());
        item.setName(report.getItemName());
        item.setLocation(report.getLocationFound());

        item.setDate(
                report.getDateReported() != null
                        ? report.getDateReported().toString()
                        : ""
        );
        item.setDateFound(item.getDate());

        item.setStudentId(report.getFinderStudentId());
        item.setContactNumber(report.getFinderContactNum());
        item.setImagePath(report.getImageUrl());

        item.setColor(cleanDescription(report.getDescription()));
        item.setTime(reportTime(report.getDescription()));

        item.setCategory(categoryName(report.getCategoryId()));

        item.setReporterName(finderName(report.getDescription()));

        item.setStatus(toStatus(report.getReportStatus()));

        return item;
    }

    private static Item.Status toStatus(String reportStatus) {
        return switch (ReportStatus.normalize(reportStatus)) {
            case ReportStatus.FOUND -> Item.Status.FOUND;
            case ReportStatus.CLAIMED -> Item.Status.CLAIMED;
            case ReportStatus.RESOLVED -> Item.Status.RESOLVED;
            default -> Item.Status.LOST;
        };
    }

    private static String categoryName(int categoryId) {
        return switch (categoryId) {
            case 1 -> "Electronics";
            case 2 -> "Bags & Wallets";
            case 3 -> "IDs & Documents";
            case 4 -> "Clothing";
            default -> "Others";
        };
    }

    private static String finderName(String description) {
        if (description == null || !description.startsWith("Finder:")) {
            return "";
        }

        int lineEnd = description.indexOf(System.lineSeparator());
        if (lineEnd < 0) {
            lineEnd = description.indexOf('\n');
        }

        String firstLine = lineEnd >= 0 ? description.substring(0, lineEnd) : description;
        return firstLine.replaceFirst("Finder:\\s*", "").trim();
    }

    private static String cleanDescription(String description) {
        if (description == null || !description.startsWith("Finder:")) {
            return description;
        }

        String[] lines = description.split("\\R");
        StringBuilder clean = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("Finder:") || line.startsWith("Report Time:")) {
                continue;
            }
            if (!clean.isEmpty()) {
                clean.append(System.lineSeparator());
            }
            clean.append(line);
        }

        return clean.toString().trim();
    }

    private static String reportTime(String description) {
        if (description == null || description.isBlank()) {
            return "";
        }

        for (String line : description.split("\\R")) {
            if (line.startsWith("Report Time:")) {
                return line.replaceFirst("Report Time:\\s*", "").trim();
            }
        }
        return "";
    }
}
