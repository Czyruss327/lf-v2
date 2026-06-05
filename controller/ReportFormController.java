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
import model.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * ReportFormController
 * Figure 1: Admin opens "New Post" form → inputs item descriptions,
 * category, and location found → system saves to ItemStore → posted
 * to public dashboard as LOST.
 */
public class ReportFormController implements Initializable {

    @FXML private ImageView logoImage;
    @FXML private Button     menuButton;
    @FXML private TextField  itemNameField;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private TextArea   descriptionArea;
    @FXML private VBox       imageListBox;
    @FXML private TextField  reporterNameField;
    @FXML private TextField  studentIdField;
    @FXML private TextField  contactField;
    @FXML private TextField  locationField;
    @FXML private TextField  dateFoundField;
    @FXML private ComboBox<String> statusCombo;
    @FXML private Label      errorLabel;

    private final List<File> uploadedImages = new ArrayList<>();
    private Consumer<Item>   onItemSaved;
    private NavbarHelper     navbar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        errorLabel.setText("");
        categoryCombo.getItems().addAll(
            "Bags & Wallets", "Electronics", "IDs & Documents",
            "Clothing", "School Supplies", "Keys", "Accessories", "Others"
        );
        statusCombo.getItems().addAll("UNCLAIMED", "CLAIMED");
        statusCombo.setValue("UNCLAIMED");
        navbar = new NavbarHelper(() -> (Stage) itemNameField.getScene().getWindow());
    }

    public void setOnItemSaved(Consumer<Item> callback) { this.onItemSaved = callback; }

    @FXML
    private void onUploadImages() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Item Photos");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Images", "*.png","*.jpg","*.jpeg","*.gif","*.webp"));
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null) return;
        for (File file : files) {
            if (uploadedImages.size() >= 5) break;
            uploadedImages.add(file);
            addImageRow(file, uploadedImages.size());
        }
    }

    private void addImageRow(File file, int index) {
        HBox row = new HBox(12);
        row.setStyle("-fx-background-color:#f8f4f2;-fx-border-color:#E0D6D0;" +
                     "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8 12;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ImageView thumb = new ImageView(new Image(file.toURI().toString(), 48, 48, true, true));
        thumb.setFitWidth(48); thumb.setFitHeight(48);
        Label name = new Label("Image " + index);
        name.setStyle("-fx-font-size:13px;-fx-text-fill:#1A1A1A;");
        HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);
        row.getChildren().addAll(thumb, name);
        imageListBox.getChildren().add(row);
    }

    @FXML
    private void onSaveReport() {
        errorLabel.setText("");
        if (itemNameField.getText().isBlank())   { errorLabel.setText("Item name is required.");    return; }
        if (categoryCombo.getValue() == null)    { errorLabel.setText("Please select a category."); return; }
        if (descriptionArea.getText().isBlank()) { errorLabel.setText("Description is required.");  return; }
        if (reporterNameField.getText().isBlank()){ errorLabel.setText("Reporter name is required.");return;}
        if (contactField.getText().isBlank())    { errorLabel.setText("Contact number is required.");return;}
        if (locationField.getText().isBlank())   { errorLabel.setText("Location is required.");     return; }
        if (dateFoundField.getText().isBlank())  { errorLabel.setText("Date found is required.");   return; }
        if (statusCombo.getValue() == null)      { errorLabel.setText("Please select a status.");   return; }

        Item.Status status = "CLAIMED".equals(statusCombo.getValue()) ? Item.Status.FOUND : Item.Status.LOST;
        String imagePath = uploadedImages.isEmpty() ? "" : uploadedImages.get(0).toURI().toString();

        Item newItem = new Item(0,
            itemNameField.getText().trim(), status,
            descriptionArea.getText().trim(),
            dateFoundField.getText().trim(),
            locationField.getText().trim(), imagePath);
        newItem.setCategory(categoryCombo.getValue());
        newItem.setReporterName(reporterNameField.getText().trim());
        newItem.setStudentId(studentIdField.getText().trim());
        newItem.setContactNumber(contactField.getText().trim());
        newItem.setDateFound(dateFoundField.getText().trim());

        if (onItemSaved != null) {
            onItemSaved.accept(newItem);
        } else {
            // Direct save via ItemStore (Figure 1 flow)
            ItemStore.getInstance().addItem(newItem);
        }
        navigateBack();
    }

    @FXML private void onCancel()  { navigateBack(); }
    @FXML private void onAddItem() { }
    @FXML private void onMenu()    { navbar.toggle(menuButton); }

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) itemNameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle("PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadImage(ImageView iv, String path) {
        try { URL url = getClass().getResource(path);
            if (url != null) iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {}
    }
}
