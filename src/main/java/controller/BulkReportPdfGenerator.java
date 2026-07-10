package controller;

import com.campuslf.models.ItemReport;
import com.campuslf.models.ReportStatus;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class BulkReportPdfGenerator {

    private static final double PAGE_WIDTH = 595.28;
    private static final double PAGE_HEIGHT = 841.89;
    private static final double MARGIN = 42.5;
    private static final double CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final double FOOTER_Y = 817;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    private BulkReportPdfGenerator() {
    }

    static void write(File output, String title, String subtitle, String groupBy,
                      Map<String, List<ItemReport>> groupedReports) throws IOException {
        PdfDocument doc = new PdfDocument();
        Page page = doc.newPage();
        double y = drawHeader(page, title, subtitle, groupBy);

        for (Map.Entry<String, List<ItemReport>> group : groupedReports.entrySet()) {
            if (y > 735) {
                page = doc.newPage();
                y = drawHeader(page, title, subtitle, groupBy);
            }

            y = drawSection(page, y, group.getKey() + " (" + group.getValue().size() + ")");
            y = drawTableHeader(page, y);

            for (ItemReport report : group.getValue()) {
                if (y > 760) {
                    page = doc.newPage();
                    y = drawHeader(page, title, subtitle, groupBy);
                    y = drawSection(page, y, group.getKey() + " (continued)");
                    y = drawTableHeader(page, y);
                }
                y = drawRow(page, y, report);
            }
            y += 10;
        }

        doc.write(output);
    }

    private static double drawHeader(Page page, String title, String subtitle, String groupBy) {
        double y = 36;
        page.text("POLYTECHNIC UNIVERSITY OF THE PHILIPPINES", MARGIN, y, 13, "F2", "8b0000");
        y += 18;
        page.text("Santa Rosa Campus Lost and Found Office", MARGIN, y, 10, "F2", "4a5568");
        y += 24;
        page.text(title, MARGIN, y, 16, "F2", "1f2937");
        y += 18;
        page.text("Generated: " + LocalDateTime.now().format(GENERATED_FORMAT), MARGIN, y, 9, "F1", "4a5568");
        page.text("Grouped by: " + groupBy, MARGIN + 340, y, 9, "F1", "4a5568");
        y += 15;
        page.text(subtitle, MARGIN, y, 8.5, "F1", "64748b");
        y += 13;
        page.line(MARGIN, y, MARGIN + CONTENT_WIDTH, y, 1.5, "8b0000");
        return y + 18;
    }

    private static double drawSection(Page page, double y, String title) {
        page.rect(MARGIN, y, CONTENT_WIDTH, 20.5, "990000", null);
        page.text(title, MARGIN + 6, y + 13.8, 10.5, "F2", "ffffff");
        return y + 28;
    }

    private static double drawTableHeader(Page page, double y) {
        page.rect(MARGIN, y, CONTENT_WIDTH, 18, "f3f6f9", "d7dee8");
        page.text("ID", MARGIN + 5, y + 12.5, 8, "F2", "4a5568");
        page.text("STATUS", MARGIN + 38, y + 12.5, 8, "F2", "4a5568");
        page.text("ITEM", MARGIN + 98, y + 12.5, 8, "F2", "4a5568");
        page.text("CATEGORY", MARGIN + 220, y + 12.5, 8, "F2", "4a5568");
        page.text("LOCATION", MARGIN + 320, y + 12.5, 8, "F2", "4a5568");
        page.text("DATE", MARGIN + 438, y + 12.5, 8, "F2", "4a5568");
        return y + 18;
    }

    private static double drawRow(Page page, double y, ItemReport report) {
        double rowHeight = 26;
        page.rect(MARGIN, y, CONTENT_WIDTH, rowHeight, "ffffff", "d7dee8");
        page.text(String.valueOf(report.getReportId()), MARGIN + 5, y + 15, 8, "F1", "1f2937");
        page.text(shorten(ReportStatus.normalize(report.getReportStatus()), 8), MARGIN + 38, y + 15, 8, "F1", "1f2937");
        page.text(shorten(report.getItemName(), 22), MARGIN + 98, y + 15, 8, "F1", "1f2937");
        page.text(shorten(categoryName(report.getCategoryId()), 16), MARGIN + 220, y + 15, 8, "F1", "1f2937");
        page.text(shorten(report.getLocationFound(), 20), MARGIN + 320, y + 15, 8, "F1", "1f2937");
        page.text(formatDate(report), MARGIN + 438, y + 15, 8, "F1", "1f2937");
        return y + rowHeight;
    }

    private static String formatDate(ItemReport report) {
        LocalDate date = report.getDateReported() != null ? report.getDateReported() : report.getDatePosted();
        return date == null ? "-" : date.format(DATE_FORMAT);
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

    private static String shorten(String value, int max) {
        String text = value == null || value.isBlank() ? "-" : value.replaceAll("\\s+", " ").trim();
        return text.length() <= max ? text : text.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static final class PdfDocument {
        private final List<Page> pages = new ArrayList<>();

        Page newPage() {
            Page page = new Page();
            pages.add(page);
            return page;
        }

        void write(File output) throws IOException {
            for (int i = 0; i < pages.size(); i++) {
                pages.get(i).textCentered("Page " + (i + 1) + " of " + pages.size(), PAGE_WIDTH / 2, FOOTER_Y, 8, "F1", "64748b");
            }

            ByteArrayOutputStream body = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            writeLine(body, "%PDF-1.4\n");
            offsets.add(body.size());
            writeLine(body, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");

            offsets.add(body.size());
            writeLine(body, "2 0 obj\n<< /Type /Pages /Kids [");
            for (int i = 0; i < pages.size(); i++) {
                writeLine(body, (3 + i) + " 0 R ");
            }
            writeLine(body, "] /Count " + pages.size() + " >>\nendobj\n");

            int contentStart = 3 + pages.size();
            for (int i = 0; i < pages.size(); i++) {
                offsets.add(body.size());
                writeLine(body, (3 + i) + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595.28 841.89] /Contents "
                        + (contentStart + i) + " 0 R /Resources << /Font << /F1 " + (contentStart + pages.size())
                        + " 0 R /F2 " + (contentStart + pages.size() + 1) + " 0 R >> >> >>\nendobj\n");
            }

            for (int i = 0; i < pages.size(); i++) {
                String stream = pages.get(i).content.toString();
                offsets.add(body.size());
                writeLine(body, (contentStart + i) + " 0 obj\n<< /Length "
                        + stream.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n");
                writeLine(body, stream);
                writeLine(body, "endstream\nendobj\n");
            }

            offsets.add(body.size());
            writeLine(body, (contentStart + pages.size()) + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
            offsets.add(body.size());
            writeLine(body, (contentStart + pages.size() + 1) + " 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

            int xrefOffset = body.size();
            int objectCount = contentStart + pages.size() + 2;
            writeLine(body, "xref\n0 " + objectCount + "\n0000000000 65535 f \n");
            for (int offset : offsets) {
                writeLine(body, String.format("%010d 00000 n %n", offset));
            }
            writeLine(body, "trailer\n<< /Size " + objectCount + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");

            try (FileOutputStream out = new FileOutputStream(output)) {
                body.writeTo(out);
            }
        }
    }

    private static final class Page {
        private final StringBuilder content = new StringBuilder();

        void text(String text, double x, double yTop, double size, String font, String color) {
            content.append("BT\n")
                    .append("/").append(font).append(" ").append(format(size)).append(" Tf\n")
                    .append(color(color)).append(" rg\n")
                    .append(format(x)).append(" ").append(format(PAGE_HEIGHT - yTop)).append(" Td\n")
                    .append("(").append(escape(text)).append(") Tj\n")
                    .append("ET\n");
        }

        void textCentered(String text, double centerX, double yTop, double size, String font, String color) {
            double approximateWidth = sanitize(text).length() * size * 0.26;
            text(text, centerX - approximateWidth, yTop, size, font, color);
        }

        void rect(double x, double yTop, double width, double height, String fill, String stroke) {
            if (fill != null) {
                content.append(color(fill)).append(" rg\n")
                        .append(format(x)).append(" ").append(format(PAGE_HEIGHT - yTop - height)).append(" ")
                        .append(format(width)).append(" ").append(format(height)).append(" re f\n");
            }
            if (stroke != null) {
                content.append(color(stroke)).append(" RG\n0.5 w\n")
                        .append(format(x)).append(" ").append(format(PAGE_HEIGHT - yTop - height)).append(" ")
                        .append(format(width)).append(" ").append(format(height)).append(" re S\n");
            }
        }

        void line(double x1, double y1Top, double x2, double y2Top, double width, String color) {
            content.append(color(color)).append(" RG\n")
                    .append(format(width)).append(" w\n")
                    .append(format(x1)).append(" ").append(format(PAGE_HEIGHT - y1Top)).append(" m\n")
                    .append(format(x2)).append(" ").append(format(PAGE_HEIGHT - y2Top)).append(" l S\n");
        }
    }

    private static void writeLine(ByteArrayOutputStream body, String value) throws IOException {
        body.write(value.getBytes(StandardCharsets.US_ASCII));
    }

    private static String color(String hex) {
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return format(r / 255.0) + " " + format(g / 255.0) + " " + format(b / 255.0);
    }

    private static String format(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private static String escape(String value) {
        return sanitize(value)
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)");
    }

    private static String sanitize(String value) {
        return (value == null || value.isBlank() ? "-" : value).replaceAll("[^\\x20-\\x7E]", " ");
    }
}
