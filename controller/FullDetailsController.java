package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Item;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class FullDetailsController implements Initializable {

    @FXML
    private ImageView logoImage;
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
    private ComboBox<String> statusCombo;

    private Item item;
    private NavbarHelper navbar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        categoryCombo.getItems().addAll(
                "Bags & Wallets", "Electronics", "IDs & Documents",
                "Clothing", "School Supplies", "Keys", "Accessories", "Others");
        statusCombo.getItems().addAll("UNCLAIMED", "CLAIMED");
        setAllReadOnly();
        navbar = new NavbarHelper(() -> (Stage) itemNameField.getScene().getWindow());
    }

    public void setItem(Item item) {
        this.item = item;
        itemNameField.setText(item.getName());
        descriptionArea.setText(item.getColor());
        locationField.setText(item.getLocation());
        dateFoundField.setText(item.getDate());
        statusCombo.setValue(item.getStatusLabel());
        categoryCombo.setValue(item.getCategory() != null ? item.getCategory() : "Others");
        reporterNameField.setText(item.getReporterName() != null ? item.getReporterName() : "");
        studentIdField.setText(item.getStudentId() != null ? item.getStudentId() : "");
        contactField.setText(item.getContactNumber() != null ? item.getContactNumber() : "");
        loadItemImage(item.getImagePath());
    }

    public void setDashboardController(DashboardController dc) {
        // Kept for existing navigation wiring.
    }

    @FXML
    private void onClaim() {
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
        navigateTo("/fxml/ReportForm.fxml", "Found Items Report");
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
        categoryCombo.setDisable(true);
        statusCombo.setDisable(true);
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
