package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Item;
import model.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FullDetailsController
 *
 * ADMIN  → can see everything, EDIT button is visible, can change Status only.
 * STUDENT → read-only view, EDIT button is hidden, CLAIM button is visible.
 */
public class FullDetailsController implements Initializable {

    @FXML private ImageView  logoImage;

    // Left — Item
    @FXML private ImageView  itemImage;
    @FXML private TextField  itemNameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea   descriptionArea;

    // Right — Reporter
    @FXML private TextField  reporterNameField;
    @FXML private TextField  studentIdField;
    @FXML private TextField  contactField;
    @FXML private TextField  locationField;
    @FXML private TextField  dateLostField;
    @FXML private ComboBox<String> statusCombo;

    // Buttons
    @FXML private Button editBtn;
    @FXML private Button claimBtn;

    private Item item;
    private boolean editMode = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");

        categoryCombo.getItems().addAll(
            "Bags & Wallets", "Electronics", "IDs & Documents",
            "Clothing", "School Supplies", "Keys", "Accessories", "Others"
        );
        statusCombo.getItems().addAll("LOST", "FOUND", "CLAIMED");

        // All fields are always read-only — only status dropdown changes on Edit
        setAllFieldsReadOnly();

        // Role-based visibility
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        editBtn.setVisible(isAdmin);
        editBtn.setManaged(isAdmin);

        // Students can claim; admins can too but it's less common
        claimBtn.setVisible(true);
    }

    public void setItem(Item item) {
        this.item = item;

        itemNameField.setText(item.getName());
        descriptionArea.setText(item.getColor());
        locationField.setText(item.getLocation());
        dateLostField.setText(item.getDate());
        statusCombo.setValue(item.getStatusLabel());
        categoryCombo.setValue(item.getCategory() != null ? item.getCategory() : "Bags & Wallets");

        reporterNameField.setText(item.getReporterName()   != null ? item.getReporterName()   : "");
        studentIdField.setText(item.getStudentId()         != null ? item.getStudentId()       : "");
        contactField.setText(item.getContactNumber()       != null ? item.getContactNumber()   : "");

        loadItemImage(item.getImagePath());
    }

    // ── Button Actions ────────────────────────────────────────

    @FXML
    private void onEdit() {
        if (!SessionManager.getInstance().isAdmin()) return; // safety check

        if (!editMode) {
            // Enter edit mode — ONLY status dropdown is enabled
            statusCombo.setDisable(false);
            editBtn.setText("SAVE");
            editMode = true;
        } else {
            // Save — apply status change to item
            if (item != null && statusCombo.getValue() != null) {
                switch (statusCombo.getValue()) {
                    case "LOST"    -> item.setStatus(Item.Status.LOST);
                    case "FOUND"   -> item.setStatus(Item.Status.FOUND);
                    case "CLAIMED" -> item.setStatus(Item.Status.LOST); // or add CLAIMED to enum
                }
            }
            statusCombo.setDisable(true);
            editBtn.setText("EDIT");
            editMode = false;

            showAlert(Alert.AlertType.INFORMATION, "Saved", "Status updated successfully.");
        }
    }

    @FXML
    private void onClaim() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/ClaimVerification.fxml"));
            Parent root = loader.load();
            ClaimVerificationController ctrl = loader.getController();
            ctrl.setItem(item);
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Claim Verification – PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void onCancel()  { navigateBack(); }
    @FXML private void onAddItem() { navigateTo("/fxml/ReportForm.fxml", "Report Form"); }
    @FXML private void onMenu()    {}

    // ── Helpers ──────────────────────────────────────────────

    private void setAllFieldsReadOnly() {
        itemNameField.setEditable(false);
        descriptionArea.setEditable(false);
        reporterNameField.setEditable(false);
        studentIdField.setEditable(false);
        contactField.setEditable(false);
        locationField.setEditable(false);
        dateLostField.setEditable(false);
        categoryCombo.setDisable(true);
        statusCombo.setDisable(true); // enabled only in edit mode for admin
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 960, 700));
            stage.setTitle("PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title + " – PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadItemImage(String path) {
        if (path == null || path.isBlank()) return;
        try {
            String uri = path.startsWith("file:") ? path
                : (getClass().getResource(path) != null
                    ? getClass().getResource(path).toExternalForm() : null);
            if (uri != null) itemImage.setImage(new Image(uri, true));
        } catch (Exception ignored) {}
    }

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {}
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
