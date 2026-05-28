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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ClaimVerificationController
 * Handles the Claim Verification form.
 */
public class ClaimVerificationController implements Initializable {

    @FXML private ImageView logoImage;

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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        errorLabel.setText("");
    }

    /** Pre-fill date fields from the item being claimed. */
    public void setItem(Item item) {
        this.item = item;
        if (item != null) {
            dateLostField.setText(item.getDate());
        }
    }

    // ── Actions ──────────────────────────────────────────────

    @FXML
    private void onUploadProof() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Upload Proof of Claim");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.pdf")
        );
        Stage stage = (Stage) claimNameField.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null) return;

        for (File file : files) {
            if (proofImages.size() >= 3) break;
            proofImages.add(file);
            addProofRow(file, proofImages.size());
        }
    }

    private void addProofRow(File file, int index) {
        HBox row = new HBox(12);
        row.setStyle("-fx-background-color:#f8f4f2; -fx-border-color:#E0D6D0; " +
                     "-fx-border-radius:6; -fx-background-radius:6; -fx-padding:8 12;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label name = new Label("Proof " + index + ": " + file.getName());
        name.setStyle("-fx-font-size:12px; -fx-text-fill:#1A1A1A;");
        row.getChildren().add(name);
        proofListBox.getChildren().add(row);
    }

    @FXML
    private void onConfirmClaim() {
        errorLabel.setText("");

        if (claimNameField.getText().isBlank())    { errorLabel.setText("Name is required."); return; }
        if (contactField.getText().isBlank())       { errorLabel.setText("Contact number is required."); return; }
        if (dateLostField.getText().isBlank())      { errorLabel.setText("Date Lost is required."); return; }
        if (courseSectionField.getText().isBlank()) { errorLabel.setText("Course and Section is required."); return; }
        if (proofImages.isEmpty())                  { errorLabel.setText("Please upload proof of claim."); return; }

        // TODO: Save claim to database, mark item as CLAIMED
        System.out.println("Claim confirmed for: " + (item != null ? item.getName() : "unknown"));
        System.out.println("Claimant: " + claimNameField.getText());

        showConfirmAlert();
        navigateBack();
    }

    @FXML private void onCancel()  { navigateBack(); }
    @FXML private void onAddItem() { navigateTo("/fxml/ReportForm.fxml", "Report Form"); }
    @FXML private void onMenu()    { System.out.println("Menu"); }

    // ── Helpers ──────────────────────────────────────────────

    private void showConfirmAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Claim Submitted");
        alert.setHeaderText(null);
        alert.setContentText("Your claim has been submitted successfully!\nAn admin will review it shortly.");
        alert.showAndWait();
    }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) claimNameField.getScene().getWindow();
            stage.setScene(new Scene(root, 960, 700));
            stage.setTitle("PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) claimNameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title + " – PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {}
    }
}
