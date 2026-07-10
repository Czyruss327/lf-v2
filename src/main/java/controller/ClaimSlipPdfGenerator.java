package controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

final class ClaimSlipPdfGenerator {

    private static final double PAGE_WIDTH = 595.28;
    private static final double PAGE_HEIGHT = 841.89;
    private static final double MARGIN = 34;
    private static final double CONTENT_WIDTH = PAGE_WIDTH - (MARGIN * 2);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM / dd / yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    private ClaimSlipPdfGenerator() {
    }

    static void write(File output, ClaimSlipData data) throws IOException {
        PdfCanvas canvas = new PdfCanvas();
        drawCopy(canvas, data, 42, true);
        drawCutLine(canvas, 410);
        drawCopy(canvas, data, 430, false);
        canvas.write(output);
    }

    static ClaimSlipData data(int claimId, int itemId, String claimantName, String courseSection,
                              String contactOrEmail, String itemName, String itemDescription,
                              String adminOfficer, LocalDate dateReleased, LocalTime timeReleased) {
        int referenceId = claimId > 0 ? claimId : Math.max(0, itemId);
        return new ClaimSlipData(
                "2026-LF-" + String.format("%04d", referenceId),
                valueOrDash(claimantName).toUpperCase(),
                valueOrDash(courseSection),
                valueOrDash(contactOrEmail),
                valueOrDash(itemName).toUpperCase() + " (Item ID: #" + Math.max(0, itemId) + ")",
                valueOrDefault(itemDescription,
                        "The item has been validated against the submitted claim details and original lost item record."),
                valueOrDefault(adminOfficer, "ADMIN ACCOUNT / PROPERTY OFFICER").toUpperCase(),
                DATE_FORMAT.format(dateReleased == null ? LocalDate.now() : dateReleased),
                TIME_FORMAT.format(timeReleased == null ? LocalTime.now() : timeReleased)
        );
    }

    private static void drawCopy(PdfCanvas canvas, ClaimSlipData data, double top, boolean adminCopy) {
        double height = adminCopy ? 350 : 320;
        canvas.rect(MARGIN, top, CONTENT_WIDTH, height, null, "c8c8c8", 0.8);

        drawHeader(canvas, data, top + 16);
        double y = top + 92;
        if (adminCopy) {
            canvas.text("[ ADMIN COPY ] This form contains verification information for internal inventory auditing and security tracking purposes.",
                    79, y, 7.8, "F2", "b00000");
        } else {
            canvas.text("[ CLAIMANT'S RECEIPT ] Please keep this copy protected and in a safe place as your official proof of item recovery.",
                    91, y, 7.8, "F2", "0065c8");
        }

        y += 26;
        if (adminCopy) {
            drawField(canvas, "CLAIMANT NAME", data.claimantName(), 49, y, 126, 155);
            drawField(canvas, "DATE FILED", data.dateReleased(), 340, y, 72, 144);
            drawField(canvas, "COURSE & SECTION", data.courseSection(), 49, y + 28, 126, 155);
            drawField(canvas, "CONTACT /\nEMAIL", data.contactOrEmail(), 340, y + 28, 72, 144);

            drawItemBox(canvas, 46, y + 76, CONTENT_WIDTH - 24, 72, "VERIFIED RELEASED ITEM SPECIFICATIONS",
                    "b00000", data.itemLine(), "Unique Identifiers Match: " + data.itemDescription());

            drawField(canvas, "ID DOCUMENT\nPRESENTED", "Submitted ID / Proof", 49, y + 162, 100, 119);
            drawField(canvas, "VERIFICATION\nMETHOD", "Proof of Claim Reviewed", 276, y + 162, 92, 181);
            drawSignatures(canvas, data, top + 314, "Signature over Printed Name of Claimant",
                    "Authorized Releasing Officer Signature");
        } else {
            drawField(canvas, "CLAIMANT NAME", data.claimantName(), 49, y, 126, 200);
            drawField(canvas, "DATE\nRELEASED", data.dateReleased(), 384, y, 66, 100);
            drawField(canvas, "COURSE & SECTION", data.courseSection(), 49, y + 34, 126, 200);
            drawField(canvas, "TIME\nRELEASED", data.timeReleased(), 384, y + 34, 66, 100);

            drawItemBox(canvas, 46, y + 80, CONTENT_WIDTH - 24, 62, "RELEASED ITEM DETAILS",
                    "0065c8", data.itemLine(),
                    "The item above has been thoroughly validated and officially handed over to the rightful owner.");

            drawSignatures(canvas, data, top + 290, "Signature of Claimant",
                    "Authorized Releasing Officer Seal");
        }
    }

    private static void drawHeader(PdfCanvas canvas, ClaimSlipData data, double y) {
        canvas.circle(76, y + 34, 20, "990000");
        canvas.text("P", 70, y + 44, 17, "F2", "ffde00");
        canvas.text("POLYTECHNIC UNIVERSITY OF THE PHILIPPINES", 114, y + 22, 10, "F2", "555555");
        canvas.text("Office of the Vice President for Administration - Property Office", 114, y + 36, 8.4, "F1", "666666");
        canvas.text("LOST AND FOUND CLAIM SLIP", 114, y + 58, 14, "F2", "8b0000");

        canvas.line(407, y + 12, 407, y + 56, 2, "8b0000");
        canvas.text("Reference No:", 415, y + 22, 8, "F2", "333333");
        canvas.text(data.referenceNo(), 415, y + 38, 10, "F2", "8b0000");
        drawBarcode(canvas, 415, y + 45, 134, 14);
    }

    private static void drawField(PdfCanvas canvas, String label, String value,
                                  double x, double y, double labelWidth, double valueWidth) {
        String[] labelLines = label.split("\\n");
        for (int i = 0; i < labelLines.length; i++) {
            canvas.text(labelLines[i], x, y + (i * 12), 8.5, "F2", "444444");
        }
        List<String> lines = wrap(value, valueWidth > 150 ? 36 : 32);
        for (int i = 0; i < Math.min(lines.size(), 2); i++) {
            canvas.text(lines.get(i), x + labelWidth, y + (i * 12), 10.5, "F1", "222222");
        }
        canvas.line(x + labelWidth - 3, y + 14 + (Math.min(lines.size(), 2) - 1) * 12,
                x + labelWidth + valueWidth, y + 14 + (Math.min(lines.size(), 2) - 1) * 12, 0.5, "777777");
    }

    private static void drawItemBox(PdfCanvas canvas, double x, double y, double width, double height,
                                    String title, String color, String itemLine, String description) {
        canvas.rect(x, y, width, height, null, color, 0.8);
        canvas.text(title, x + 8, y + 16, 8.8, "F2", color);
        canvas.line(x + 8, y + 22, x + width - 8, y + 22, 0.4, "dddddd");
        canvas.text(itemLine, x + 8, y + 38, 9.8, "F2", "222222");
        double textY = y + 52;
        for (String line : wrap(description, 105)) {
            canvas.text(line, x + 8, textY, 8.2, "F1", "555555");
            textY += 10;
            if (textY > y + height - 8) {
                break;
            }
        }
    }

    private static void drawSignatures(PdfCanvas canvas, ClaimSlipData data, double y,
                                       String claimantCaption, String officerCaption) {
        canvas.line(46, y, 272, y, 0.8, "333333");
        canvas.line(324, y, 549, y, 0.8, "333333");
        canvas.textCentered(data.claimantName(), 159, y + 11, 8, "F2", "444444");
        canvas.textCentered(claimantCaption, 159, y + 22, 6.8, "F1", "777777");
        canvas.textCentered(data.adminOfficer(), 436, y + 11, 8, "F2", "444444");
        canvas.textCentered(officerCaption, 436, y + 22, 6.8, "F1", "777777");
    }

    private static void drawCutLine(PdfCanvas canvas, double y) {
        canvas.dashLine(MARGIN, y, MARGIN + CONTENT_WIDTH, y, "888888");
        canvas.text("x", 53, y + 4, 8, "F1", "555555");
        canvas.rect(174, y - 6, 248, 12, "ffffff", null, 0);
        canvas.textCentered("DETACH HERE (PORTION TO BE KEPT BY THE CLAIMANT)", PAGE_WIDTH / 2, y + 4, 8, "F2", "666666");
    }

    private static void drawBarcode(PdfCanvas canvas, double x, double y, double width, double height) {
        canvas.rect(x, y, width, height, "000000", null, 0);
        int[] bars = {2, 1, 1, 2, 1, 3, 1, 1, 2, 2, 1, 3, 1, 1, 2, 1};
        double cursor = x + 6;
        for (int i = 0; i < bars.length && cursor < x + width - 4; i++) {
            if (i % 2 == 0) {
                canvas.rect(cursor, y + 3, bars[i], height - 6, "ffffff", null, 0);
            }
            cursor += bars[i] + 4;
        }
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

    record ClaimSlipData(String referenceNo, String claimantName, String courseSection,
                         String contactOrEmail, String itemLine, String itemDescription,
                         String adminOfficer, String dateReleased, String timeReleased) {
    }

    private static final class PdfCanvas {
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

        void rect(double x, double yTop, double width, double height, String fill, String stroke, double strokeWidth) {
            if (fill != null) {
                content.append(color(fill)).append(" rg\n")
                        .append(format(x)).append(" ").append(format(PAGE_HEIGHT - yTop - height)).append(" ")
                        .append(format(width)).append(" ").append(format(height)).append(" re f\n");
            }
            if (stroke != null) {
                content.append(color(stroke)).append(" RG\n").append(format(strokeWidth)).append(" w\n")
                        .append(format(x)).append(" ").append(format(PAGE_HEIGHT - yTop - height)).append(" ")
                        .append(format(width)).append(" ").append(format(height)).append(" re S\n");
            }
        }

        void circle(double centerX, double centerYTop, double radius, String fill) {
            double c = radius * 0.5522847498;
            double cy = PAGE_HEIGHT - centerYTop;
            content.append(color(fill)).append(" rg\n")
                    .append(format(centerX)).append(" ").append(format(cy + radius)).append(" m\n")
                    .append(format(centerX + c)).append(" ").append(format(cy + radius)).append(" ")
                    .append(format(centerX + radius)).append(" ").append(format(cy + c)).append(" ")
                    .append(format(centerX + radius)).append(" ").append(format(cy)).append(" c\n")
                    .append(format(centerX + radius)).append(" ").append(format(cy - c)).append(" ")
                    .append(format(centerX + c)).append(" ").append(format(cy - radius)).append(" ")
                    .append(format(centerX)).append(" ").append(format(cy - radius)).append(" c\n")
                    .append(format(centerX - c)).append(" ").append(format(cy - radius)).append(" ")
                    .append(format(centerX - radius)).append(" ").append(format(cy - c)).append(" ")
                    .append(format(centerX - radius)).append(" ").append(format(cy)).append(" c\n")
                    .append(format(centerX - radius)).append(" ").append(format(cy + c)).append(" ")
                    .append(format(centerX - c)).append(" ").append(format(cy + radius)).append(" ")
                    .append(format(centerX)).append(" ").append(format(cy + radius)).append(" c f\n");
        }

        void line(double x1, double y1Top, double x2, double y2Top, double width, String color) {
            content.append(color(color)).append(" RG\n")
                    .append(format(width)).append(" w\n")
                    .append(format(x1)).append(" ").append(format(PAGE_HEIGHT - y1Top)).append(" m\n")
                    .append(format(x2)).append(" ").append(format(PAGE_HEIGHT - y2Top)).append(" l S\n");
        }

        void dashLine(double x1, double y1Top, double x2, double y2Top, String color) {
            content.append("[4 5] 0 d\n");
            line(x1, y1Top, x2, y2Top, 1.2, color);
            content.append("[] 0 d\n");
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
            writeLine(body, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595.28 841.89] /Contents 4 0 R /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> >>\nendobj\n");

            String stream = content.toString();
            offsets.add(body.size());
            writeLine(body, "4 0 obj\n<< /Length " + stream.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n");
            writeLine(body, stream);
            writeLine(body, "endstream\nendobj\n");
            offsets.add(body.size());
            writeLine(body, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");
            offsets.add(body.size());
            writeLine(body, "6 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>\nendobj\n");

            int xrefOffset = body.size();
            writeLine(body, "xref\n0 7\n0000000000 65535 f \n");
            for (int offset : offsets) {
                writeLine(body, String.format("%010d 00000 n %n", offset));
            }
            writeLine(body, "trailer\n<< /Size 7 /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");

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
}
