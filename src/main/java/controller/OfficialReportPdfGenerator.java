package controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

final class OfficialReportPdfGenerator {

    private static final double PAGE_WIDTH = 595.28;
    private static final double PAGE_HEIGHT = 841.89;
    private static final double MARGIN = 42.5;
    private static final double CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter GENERATED_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");

    private OfficialReportPdfGenerator() {
    }

    static void write(File output, ReportData data) throws IOException {
        PdfCanvas canvas = new PdfCanvas();
        drawReport(canvas, data);
        canvas.write(output);
    }

    static ReportData data(boolean foundReport, int reportId, String itemName, String category,
                           String location, LocalDate reportDate, String reportDateText,
                           String time, String description, String reporterName,
                           String emailAddress, String contactNumber, boolean anonymousFinder,
                           String imageLabelOne, String imageLabelTwo, List<String> imagePaths) {
        String prefix = foundReport ? "FI" : "LR";
        int id = reportId > 0 ? reportId : Math.abs((itemName + LocalDate.now()).hashCode() % 10000);
        String caseId = String.format("%s-%d-%04d", prefix, LocalDate.now().getYear(), id);
        String dateText = reportDate != null ? DATE_FORMAT.format(reportDate) : valueOrDash(reportDateText);

        return new ReportData(
                foundReport,
                caseId,
                LocalDateTime.now().format(GENERATED_FORMAT),
                valueOrDash(itemName),
                valueOrDash(category),
                valueOrDash(location),
                dateText,
                valueOrDash(time),
                valueOrDash(description),
                valueOrDash(reporterName),
                valueOrDash(emailAddress),
                valueOrDash(contactNumber),
                anonymousFinder,
                valueOrDefault(imageLabelOne, "[" + valueOrDash(itemName) + " - Front View]"),
                valueOrDefault(imageLabelTwo, "[" + valueOrDash(itemName) + " - Alternate Angle]"),
                imagePaths == null ? List.of() : imagePaths.stream().limit(2).toList()
        );
    }

    private static void drawReport(PdfCanvas canvas, ReportData data) {
        double y = 36;
        canvas.text("POLYTECHNIC UNIVERSITY OF THE PHILIPPINES", MARGIN, y, 13, "F2", "8b0000");
        y += 18;
        canvas.text("Santa Rosa Campus Lost and Found Office", MARGIN, y, 10, "F2", "4a5568");
        y += 24;
        canvas.text("OFFICIAL " + data.reportType() + " ITEM REPORT", MARGIN, y, 16, "F2", "1f2937");
        y += 20;
        canvas.line(MARGIN, y, MARGIN + CONTENT_WIDTH, y, 1.5, "8b0000");
        y += 20;

        canvas.text("Case ID:", MARGIN, y, 9.5, "F2", "4a5568");
        canvas.text(data.caseId(), MARGIN + 76, y, 9.5, "F1", "1f2937");
        canvas.text("Generated:", MARGIN + 335, y, 9.5, "F2", "4a5568");
        canvas.text(data.generatedAt(), MARGIN + 425, y, 9.5, "F1", "1f2937");
        y += 23;

        y = drawSection(canvas, y, data.reportType() + " ITEM DETAILS");
        List<Row> itemRows = List.of(
                new Row("Item Name", data.itemName()),
                new Row("Category", data.category()),
                new Row("Location " + data.reportTypeTitle(), data.location()),
                new Row("Date " + data.reportTypeTitle(), data.reportDate()),
                new Row("Time " + data.reportTypeTitle(), data.time())
        );
        y = drawTable(canvas, y, itemRows);
        y += 10;

        y = drawSection(canvas, y, "ITEM DESCRIPTION & DISTINCT MARKS");
        y = drawTextBox(canvas, y, data.description());
        y += 10;

        y = drawSection(canvas, y, data.foundReport() ? "REPORTER / FINDER INFORMATION" : "REPORTER INFORMATION");
        if (data.foundReport()) {
            y = drawParagraph(canvas, y,
                    "Privacy Disclaimer: Providing finder's details is completely voluntary. If shared, they are strictly confidential, accessible only to Admin for verification, and never posted publicly on the blog site.",
                    8.4, "F3", "64748b", 88);
            y += 5;
            canvas.text("[x]  " + (data.anonymousFinder() ? "Keep Finder Completely Anonymous" : "Finder Details Recorded"),
                    MARGIN, y, 9.5, "F2", "1f2937");
            y += 16;
            y = drawTable(canvas, y, List.of(
                    new Row("Finder's Name", data.anonymousFinder() ? "N/A (Opted Anonymous)" : data.reporterName()),
                    new Row("Contact Number", data.anonymousFinder() ? "N/A (Opted Anonymous)" : data.contactNumber())
            ));
        } else {
            y = drawTable(canvas, y, List.of(
                    new Row("Reporter Name", data.reporterName()),
                    new Row("Email Address", data.emailAddress()),
                    new Row("Contact Number", data.contactNumber())
            ));
        }
        y += 10;

        y = drawSection(canvas, y, data.foundReport() ? "ATTACHED REFERENCE IMAGES" : "ATTACHED EVIDENCE / REFERENCE IMAGES");
        double afterImagesY = drawImagePlaceholders(canvas, y, data);

        double footerY = afterImagesY + 14;
        canvas.dashLine(MARGIN, footerY - 2, MARGIN + CONTENT_WIDTH, footerY - 2, "cbd5e1");
        canvas.text(data.foundReport()
                        ? "PUPSRC LOST AND FOUND SYSTEM ADMIN ARCHIVAL COPY"
                        : "PUPSRC LOST AND FOUND SYSTEM CONFIDENTIAL DOCUMENT",
                MARGIN, footerY + 12, 9, "F2", "4a5568");
        String footerText = data.foundReport()
                ? "This document serves as an official database record archive for the PUPSRC Lost and Found Center. Confidentiality flags are applied based on the reporter's anonymity preference selection."
                : "This document is an automatic archival copy generated directly from the PUPSRC Lost and Found Management Platform. All information is classified for university property documentation purposes only.";
        drawParagraph(canvas, footerY + 25, footerText, 8, "F1", "64748b", 120);
        canvas.textCentered("Page 1 of 1", PAGE_WIDTH / 2, 817, 8, "F1", "64748b");
    }

    private static double drawSection(PdfCanvas canvas, double y, String title) {
        canvas.rect(MARGIN, y, CONTENT_WIDTH, 20.5, "990000", null);
        canvas.text(title, MARGIN + 6, y + 13.8, 11, "F2", "ffffff");
        return y + 29;
    }

    private static double drawTable(PdfCanvas canvas, double y, List<Row> rows) {
        double labelWidth = 128;
        for (Row row : rows) {
            List<String> wrapped = wrap(row.value(), 68);
            double rowHeight = Math.max(21.5, 13 + (wrapped.size() * 10));
            canvas.rect(MARGIN, y, labelWidth, rowHeight, "f3f6f9", "d7dee8");
            canvas.rect(MARGIN + labelWidth, y, CONTENT_WIDTH - labelWidth, rowHeight, "ffffff", "d7dee8");
            canvas.text(row.label(), MARGIN + 6, y + 15, 9.5, "F2", "4a5568");
            double valueY = y + 15;
            for (String line : wrapped) {
                canvas.text(line, MARGIN + labelWidth + 6, valueY, 9.5, "F1", "1f2937");
                valueY += 10;
            }
            y += rowHeight;
        }
        return y;
    }

    private static double drawTextBox(PdfCanvas canvas, double y, String text) {
        List<String> wrapped = wrap(text, 105);
        double height = Math.max(39, 14 + (wrapped.size() * 12));
        canvas.rect(MARGIN, y, CONTENT_WIDTH, height, "f8fafc", "d7dee8");
        double textY = y + 15;
        for (String line : wrapped) {
            canvas.text(line, MARGIN + 8, textY, 9.5, "F1", "1f2937");
            textY += 12;
        }
        return y + height;
    }

    private static double drawParagraph(PdfCanvas canvas, double y, String text, double size,
                                        String font, String color, int maxChars) {
        for (String line : wrap(text, maxChars)) {
            canvas.text(line, MARGIN, y, size, font, color);
            y += size + 3;
        }
        return y;
    }

    private static double drawImagePlaceholders(PdfCanvas canvas, double y, ReportData data) {
        double gap = 10;
        double boxWidth = (CONTENT_WIDTH - gap) / 2;
        double boxHeight = 112;
        String imageCaption = data.foundReport() ? "Saved Item Photo" : "Submitted Reference Photo";
        canvas.text("Image 1: " + (data.imagePaths().isEmpty() ? "No Reference Image" : imageCaption),
                MARGIN, y + 11, 8.7, "F2", "4a5568");
        canvas.text("Image 2: " + (data.imagePaths().size() < 2 ? "No Reference Image" : imageCaption),
                MARGIN + boxWidth + gap, y + 11, 8.7, "F2", "4a5568");
        canvas.dashRect(MARGIN, y + 18, boxWidth, boxHeight, "f8fafc", "cbd5e1");
        canvas.dashRect(MARGIN + boxWidth + gap, y + 18, boxWidth, boxHeight, "f8fafc", "cbd5e1");

        drawImageSlot(canvas, data.imagePaths(), 0, MARGIN, y + 18, boxWidth, boxHeight, data.imageLabelOne());
        drawImageSlot(canvas, data.imagePaths(), 1, MARGIN + boxWidth + gap, y + 18, boxWidth, boxHeight, data.imageLabelTwo());
        return y + 18 + boxHeight;
    }

    private static void drawImageSlot(PdfCanvas canvas, List<String> imagePaths, int index,
                                      double x, double y, double width, double height, String fallbackLabel) {
        String imagePath = index < imagePaths.size() ? imagePaths.get(index) : "";
        if (canvas.image(imagePath, x + 4, y + 4, width - 8, height - 8)) {
            return;
        }

        String label = index == 0 ? fallbackLabel : "No Reference Image";
        canvas.textCentered(label, x + (width / 2), y + 12, 8.8, "F1", "64748b");
    }

    private static List<String> wrap(String value, int maxLength) {
        List<String> lines = new ArrayList<>();
        String remaining = valueOrDash(value).replaceAll("\\s+", " ").trim();
        while (remaining.length() > maxLength) {
            int split = remaining.lastIndexOf(' ', maxLength);
            if (split <= 0) {
                split = maxLength;
            }
            lines.add(remaining.substring(0, split).trim());
            remaining = remaining.substring(split).trim();
        }
        if (!remaining.isBlank()) {
            lines.add(remaining);
        }
        return lines.isEmpty() ? List.of("-") : lines;
    }

    private static String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    record ReportData(boolean foundReport, String caseId, String generatedAt, String itemName,
                      String category, String location, String reportDate, String time,
                      String description, String reporterName, String emailAddress,
                      String contactNumber, boolean anonymousFinder, String imageLabelOne,
                      String imageLabelTwo, List<String> imagePaths) {

        String reportType() {
            return foundReport ? "FOUND" : "LOST";
        }

        String reportTypeTitle() {
            return foundReport ? "Found" : "Lost";
        }
    }

    private record Row(String label, String value) {
    }

    private static final class PdfCanvas {
        private final StringBuilder content = new StringBuilder();
        private final List<ImageResource> images = new ArrayList<>();

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

        void dashLine(double x1, double y1Top, double x2, double y2Top, String color) {
            content.append("[3 3] 0 d\n");
            line(x1, y1Top, x2, y2Top, 0.7, color);
            content.append("[] 0 d\n");
        }

        void dashRect(double x, double yTop, double width, double height, String fill, String stroke) {
            content.append("[3 3] 0 d\n");
            rect(x, yTop, width, height, fill, stroke);
            content.append("[] 0 d\n");
        }

        boolean image(String path, double x, double yTop, double boxWidth, double boxHeight) {
            try {
                ImageResource image = ImageResource.from(path, "Im" + (images.size() + 1));
                if (image == null) {
                    return false;
                }
                images.add(image);

                double scale = Math.min(boxWidth / image.width(), boxHeight / image.height());
                double drawWidth = image.width() * scale;
                double drawHeight = image.height() * scale;
                double drawX = x + ((boxWidth - drawWidth) / 2);
                double drawY = PAGE_HEIGHT - yTop - ((boxHeight - drawHeight) / 2) - drawHeight;

                content.append("q\n")
                        .append(format(drawWidth)).append(" 0 0 ").append(format(drawHeight)).append(" ")
                        .append(format(drawX)).append(" ").append(format(drawY)).append(" cm\n")
                        .append("/").append(image.name()).append(" Do\n")
                        .append("Q\n");
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        void write(File output) throws IOException {
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            List<Integer> offsets = new ArrayList<>();
            writeLine(body, "%PDF-1.4\n");
            offsets.add(body.size());
            writeLine(body, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
            offsets.add(body.size());
            writeLine(body, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
            offsets.add(body.size());
            writeLine(body, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595.28 841.89] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R /F3 7 0 R >>");
            if (!images.isEmpty()) {
                writeLine(body, " /XObject <<");
                for (int i = 0; i < images.size(); i++) {
                    writeLine(body, " /" + images.get(i).name() + " " + (8 + i) + " 0 R");
                }
                writeLine(body, " >>");
            }
            writeLine(body, " >> >>\nendobj\n");

            String stream = content.toString();
            offsets.add(body.size());
            writeLine(body, "4 0 obj\n<< /Length " + stream.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n");
            writeLine(body, stream);
            writeLine(body, "endstream\nendobj\n");
            offsets.add(body.size());
            writeLine(body, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
            offsets.add(body.size());
            writeLine(body, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");
            offsets.add(body.size());
            writeLine(body, "7 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Oblique >>\nendobj\n");
            for (ImageResource image : images) {
                offsets.add(body.size());
                writeLine(body, (7 + offsets.size() - 7) + " 0 obj\n");
                writeLine(body, "<< /Type /XObject /Subtype /Image /Width " + image.width()
                        + " /Height " + image.height()
                        + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /DCTDecode /Length "
                        + image.bytes().length + " >>\nstream\n");
                body.write(image.bytes());
                writeLine(body, "\nendstream\nendobj\n");
            }

            int xrefOffset = body.size();
            writeLine(body, "xref\n0 " + (8 + images.size()) + "\n0000000000 65535 f \n");
            for (int offset : offsets) {
                writeLine(body, String.format("%010d 00000 n %n", offset));
            }
            writeLine(body, "trailer\n<< /Size " + (8 + images.size()) + " /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");

            try (FileOutputStream out = new FileOutputStream(output)) {
                body.writeTo(out);
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
            return valueOrDash(value).replaceAll("[^\\x20-\\x7E]", " ");
        }
    }

    private record ImageResource(String name, byte[] bytes, int width, int height) {
        static ImageResource from(String path, String name) throws IOException {
            File file = resolveFile(path);
            if (file == null || !file.isFile()) {
                return null;
            }

            BufferedImage source = ImageIO.read(file);
            if (source == null) {
                return null;
            }

            BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = rgb.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, rgb.getWidth(), rgb.getHeight());
            graphics.drawImage(source, 0, 0, null);
            graphics.dispose();

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ImageIO.write(rgb, "jpg", bytes);
            return new ImageResource(name, bytes.toByteArray(), rgb.getWidth(), rgb.getHeight());
        }

        private static File resolveFile(String path) {
            if (path == null || path.isBlank()) {
                return null;
            }
            try {
                if (path.startsWith("file:")) {
                    return new File(new URI(path));
                }
            } catch (IllegalArgumentException | URISyntaxException ignored) {
                return null;
            }
            return new File(path);
        }
    }
}
