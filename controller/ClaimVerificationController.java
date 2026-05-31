package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Item;
import model.ItemStore;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


/**
 * ClaimVerificationController
 * Figure 2 YES branch:
 *  1. Admin opens Claimant verification form and inputs claimant's info.
 *  2. System saves information in the audit logs and updates status as FOUND.
 *  3. System removes item's post from the public dashboard.
 *  4. Admin hands over physical item to the claimant.
 */
public class ClaimVerificationController implements Initializable {

    @FXML private ImageView logoImage;
    @FXML private Button    menuButton;
    @FXML private TextField claimNameField;
    @FXML private TextField studentIdField;
    @FXML private TextField contactField;
    @FXML private TextField dateLostField;
    @FXML private TextField dateFoundField;
    @FXML private TextField courseSectionField;
    @FXML private VBox      proofListBox;
    @FXML private Label     errorLabel;

    private Item item;
    private final List<File> proofImages = new ArrayList<>();
    private NavbarHelper navbar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        errorLabel.setText("");
        navbar = new NavbarHelper(() -> (Stage) claimNameField.getScene().getWindow());
    }

    public void setItem(Item item) {
        this.item = item;
        if (item != null) dateLostField.setText(item.getDate());
    }

    @FXML
    private void onUploadProof() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload Proof of Claim");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images / PDF", "*.png","*.jpg","*.jpeg","*.pdf"));
        Stage stage = (Stage) claimNameField.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null) return;
        for (File file : files) {
            if (proofImages.size() >= 3) break;
            proofImages.add(file);
            HBox row = new HBox(12);
            row.setStyle("-fx-background-color:#f8f4f2;-fx-border-color:#E0D6D0;" +
                         "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8 12;");
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label name = new Label("Proof " + proofImages.size() + ": " + file.getName());
            name.setStyle("-fx-font-size:12px;-fx-text-fill:#1A1A1A;");
            row.getChildren().add(name);
            proofListBox.getChildren().add(row);
        }
    }

    @FXML
    private void onConfirmClaim() {
        errorLabel.setText("");
        if (claimNameField.getText().isBlank())    { errorLabel.setText("Name is required.");                  return; }
        if (contactField.getText().isBlank())       { errorLabel.setText("Contact number is required.");       return; }
        if (dateLostField.getText().isBlank())      { errorLabel.setText("Date Lost is required.");            return; }
        if (courseSectionField.getText().isBlank()) { errorLabel.setText("Course and Section is required.");   return; }
        if (proofImages.isEmpty())                  { errorLabel.setText("Please upload proof of claim.");     return; }

        if (item != null) {
            // Figure 2: system saves to audit logs, updates status FOUND,
            // removes item's post from public dashboard view.
            ItemStore.getInstance().markAsClaimed(item, claimNameField.getText().trim());
        }

        showConfirmAndGoBack();
    }

    private void showConfirmAndGoBack() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Claim Confirmed");
        alert.setHeaderText(null);
        alert.setContentText(
            "Claim confirmed!\n\n" +
            "• Audit log updated\n" +
            "• Item status set to FOUND\n" +
            "• Item removed from public dashboard\n\n" +
            "Please hand over the physical item to the claimant.");
        alert.showAndWait();
        navigateBack();
    }

    @FXML private void onCancel()  { navigateBack(); }
    @FXML private void onAddItem() { navigateTo("/fxml/ReportForm.fxml", "New Post"); }
    @FXML private void onMenu()    { navbar.toggle(menuButton); }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) claimNameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle("PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateTo(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) claimNameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle(title + " – PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadImage(ImageView iv, String path) {
        try { URL url = getClass().getResource(path);
            if (url != null) iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {}
    }
}
