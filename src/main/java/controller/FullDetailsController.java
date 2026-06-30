package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Item;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class FullDetailsController implements Initializable {

    @FXML
    private ImageView logoImage;
    @FXML
    private Button addButton;
    @FXML
    private Button menuButton;
    @FXML
    private ImageView itemImage;
    @FXML
    private TextField itemNameField;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField reporterNameField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField dateFoundField;
    @FXML
    private TextField timeFoundField;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private Button claimBtn;

    private Item item;
    private NavbarHelper navbar;
    private ReportMenuHelper reportMenu;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        categoryCombo.getItems().addAll(
                "Bags & Wallets", "Electronics", "IDs & Documents",
                "Clothing", "School Supplies", "Keys", "Accessories", "Others");
        statusCombo.getItems().addAll("UNCLAIMED", "CLAIMED");
        setAllReadOnly();
        navbar = new NavbarHelper(() -> (Stage) itemNameField.getScene().getWindow());
        reportMenu = new ReportMenuHelper(() -> (Stage) itemNameField.getScene().getWindow());
    }

    public void setItem(Item item) {
        this.item = item;
        itemNameField.setText(item.getName());
        descriptionArea.setText(item.getColor());
        locationField.setText(item.getLocation());
        dateFoundField.setText(item.getDate());
        timeFoundField.setText("");
        statusCombo.setValue(item.getStatusLabel());
        categoryCombo.setValue(item.getCategory() != null ? item.getCategory() : "Others");
        reporterNameField.setText(item.getReporterName() != null ? item.getReporterName() : "");
        studentIdField.setText(item.getStudentId() != null ? item.getStudentId() : "");
        contactField.setText(item.getContactNumber() != null ? item.getContactNumber() : "");
        loadItemImage(item.getImagePath());
        updateClaimButtonState();
    }

    public void setDashboardController(DashboardController dc) {
        // Kept for existing navigation wiring.
    }

    @FXML
    private void onClaim() {
        if (item == null || item.getStatus() == Item.Status.FOUND) {
            showAlert("Already Claimed", "This item has already been claimed.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClaimVerification.fxml"));
            Parent root = loader.load();
            ClaimVerificationController ctrl = loader.getController();
            ctrl.setItem(item);
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle("Claim Verification - PUPSRC Lost and Found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onCancel() {
        navigateBack();
    }

    @FXML
    private void onAddItem() {
        reportMenu.toggle(addButton);
    }

    @FXML
    private void onMenu() {
        navbar.toggle(menuButton);
    }

    private void setAllReadOnly() {
        itemNameField.setEditable(false);
        descriptionArea.setEditable(false);
        reporterNameField.setEditable(false);
        studentIdField.setEditable(false);
        contactField.setEditable(false);
        locationField.setEditable(false);
        dateFoundField.setEditable(false);
        timeFoundField.setEditable(false);
        categoryCombo.setDisable(true);
        statusCombo.setDisable(true);
    }

    @FXML
    private void onGeneratePdf() {
        if (item == null) {
            showAlert("No Item Selected", "Open an item before generating a PDF.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Blog Site PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName(buildPdfFileName(item.getName()));
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        File output = chooser.showSaveDialog(stage);
        if (output == null) {
            return;
        }

        try {
            writeSimplePdf(output, buildPdfLines());
            showInfo("PDF Generated", "The blog-site PDF has been saved.");
        } catch (IOException e) {
            showAlert("PDF Error", "Unable to generate the PDF file.");
        }
    }

    private void updateClaimButtonState() {
        boolean alreadyClaimed = item != null && item.getStatus() == Item.Status.FOUND;
        claimBtn.setDisable(alreadyClaimed);
        claimBtn.setText(alreadyClaimed ? "CLAIMED" : "CLAIM");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private List<String> buildPdfLines() {
        List<String> lines = new ArrayList<>();
        lines.add("LOST ITEM DETAILS");
        lines.add("PUPSRC Lost and Found");
        lines.add("");
        lines.add("Item Name: " + valueOrDash(item.getName()));
        lines.add("Category: " + valueOrDash(item.getCategory()));
        lines.add("Description: " + valueOrDash(item.getColor()));
        lines.add("Location Lost / Found: " + valueOrDash(item.getLocation()));
        lines.add("Date Lost / Found: " + valueOrDash(item.getDate()));
        if (!timeFoundField.getText().isBlank()) {
            lines.add("Time Lost / Found: " + timeFoundField.getText().trim());
        }
        lines.add("");
        lines.add("Reporter: " + valueOrDash(item.getReporterName()));
        lines.add("Student ID: " + valueOrDash(item.getStudentId()));
        lines.add("Contact Number: " + valueOrDash(item.getContactNumber()));
        lines.add("Status: " + valueOrDash(item.getStatusLabel()));
        return lines;
    }

    private void writeSimplePdf(File output, List<String> lines) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        writePdfLine(body, "%PDF-1.4\n");
        offsets.add(body.size());
        writePdfLine(body, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        offsets.add(body.size());
        writePdfLine(body, "2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n");
        offsets.add(body.size());
        writePdfLine(body, "3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>\nendobj\n");

        String stream = buildPdfContentStream(lines);
        offsets.add(body.size());
        writePdfLine(body, "4 0 obj\n<< /Length " + stream.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n");
        writePdfLine(body, stream);
        writePdfLine(body, "endstream\nendobj\n");
        offsets.add(body.size());
        writePdfLine(body, "5 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n");

        int xrefOffset = body.size();
        writePdfLine(body, "xref\n0 6\n0000000000 65535 f \n");
        for (int offset : offsets) {
            writePdfLine(body, String.format("%010d 00000 n %n", offset));
        }
        writePdfLine(body, "trailer\n<< /Size 6 /Root 1 0 R >>\nstartxref\n" + xrefOffset + "\n%%EOF\n");

        try (FileOutputStream out = new FileOutputStream(output)) {
            body.writeTo(out);
        }
    }

    private String buildPdfContentStream(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        sb.append("BT\n/F1 18 Tf\n72 730 Td\n(").append(escapePdf(lines.get(0))).append(") Tj\n");
        sb.append("/F1 11 Tf\n0 -28 Td\n");
        for (int i = 1; i < lines.size(); i++) {
            for (String wrapped : wrapLine(lines.get(i), 78)) {
                sb.append("(").append(escapePdf(wrapped)).append(") Tj\n0 -18 Td\n");
            }
        }
        sb.append("ET\n");
        return sb.toString();
    }

    private List<String> wrapLine(String line, int maxLength) {
        List<String> result = new ArrayList<>();
        if (line.length() <= maxLength) {
            result.add(line);
            return result;
        }
        String remaining = line;
        while (remaining.length() > maxLength) {
            int split = remaining.lastIndexOf(' ', maxLength);
            if (split <= 0) {
                split = maxLength;
            }
            result.add(remaining.substring(0, split).trim());
            remaining = remaining.substring(split).trim();
        }
        if (!remaining.isBlank()) {
            result.add(remaining);
        }
        return result;
    }

    private String escapePdf(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replaceAll("[^\\x20-\\x7E]", " ");
    }

    private void writePdfLine(ByteArrayOutputStream body, String value) throws IOException {
        body.write(value.getBytes(StandardCharsets.US_ASCII));
    }

    private String buildPdfFileName(String itemName) {
        String safeName = itemName == null ? "item-details" : itemName.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        safeName = safeName.replaceAll("^-|-$", "");
        if (safeName.isBlank()) {
            safeName = "item-details";
        }
        return safeName + "-blog-details.pdf";
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle("PUPSRC Lost and Found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            if ("/fxml/ReportForm.fxml".equals(path)) {
                ReportFormController ctrl = loader.getController();
                ctrl.setFoundReportMode();
            }
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle(title + " - PUPSRC Lost and Found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadItemImage(String path) {
        if (path == null || path.isBlank())
            return;
        try {
            URL resource = getClass().getResource(path);
            String uri = path.startsWith("file:") ? path : (resource != null ? resource.toExternalForm() : null);
            if (uri != null)
                itemImage.setImage(new Image(uri, true));
        } catch (Exception ignored) {
        }
    }

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null)
                iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {
        }
    }
}
