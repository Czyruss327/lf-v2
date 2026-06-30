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

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
            OfficialReportPdfGenerator.write(output, buildOfficialReportData());
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

    private OfficialReportPdfGenerator.ReportData buildOfficialReportData() {
        boolean foundReport = item.getStatus() == Item.Status.FOUND;
        boolean anonymousFinder = foundReport && valueOrDash(item.getReporterName()).equals("-");
        return OfficialReportPdfGenerator.data(
                foundReport,
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getLocation(),
                null,
                item.getDate(),
                timeFoundField.getText(),
                item.getColor(),
                item.getReporterName(),
                "",
                item.getContactNumber(),
                anonymousFinder,
                "[" + valueOrDash(item.getName()) + " - Front View]",
                "[" + valueOrDash(item.getName()) + " - Alternate Angle]"
        );
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
