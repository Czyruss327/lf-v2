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
import java.time.format.DateTimeFormatter;
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

    @FXML
    private ImageView logoImage;
    @FXML
    private Button menuButton;
    @FXML
    private TextField itemNameField;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private VBox imageListBox;
    @FXML
    private TextField reporterNameField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField locationField;
    @FXML
    private DatePicker dateFoundPicker;
    @FXML
    private Label errorLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final List<File> uploadedImages = new ArrayList<>();
    private Consumer<Item> onItemSaved;
    private NavbarHelper navbar;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        errorLabel.setText("");
        categoryCombo.getItems().addAll(
                "Bags & Wallets", "Electronics", "IDs & Documents",
                "Clothing", "School Supplies", "Keys", "Accessories", "Others");
        navbar = new NavbarHelper(() -> (Stage) itemNameField.getScene().getWindow());
    }

    public void setOnItemSaved(Consumer<Item> callback) {
        this.onItemSaved = callback;
    }

    @FXML
    private void onUploadImages() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Item Photos");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null)
            return;
        for (File file : files) {
            if (uploadedImages.size() >= 5)
                break;
            uploadedImages.add(file);
            refreshImageList();
        }
    }

    private void refreshImageList() {
        imageListBox.getChildren().clear();
        for (int i = 0; i < uploadedImages.size(); i++) {
            addImageRow(uploadedImages.get(i), i);
        }
    }

    private void addImageRow(File file, int index) {
        HBox row = new HBox(12);
        row.setStyle("-fx-background-color:#f8f4f2;-fx-border-color:#E0D6D0;" +
                "-fx-border-radius:6;-fx-background-radius:6;-fx-padding:8 12;");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ImageView thumb = new ImageView(new Image(file.toURI().toString(), 48, 48, true, true));
        thumb.setFitWidth(48);
        thumb.setFitHeight(48);
        Label name = new Label("Image " + (index + 1));
        name.setStyle("-fx-font-size:13px;-fx-text-fill:#1A1A1A;");
        HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);
        Button removeButton = new Button("X");
        removeButton.setStyle("-fx-background-color:#8B0000;-fx-text-fill:white;-fx-font-weight:bold;" +
                "-fx-background-radius:14;-fx-min-width:28;-fx-min-height:28;-fx-padding:0;");
        removeButton.setOnAction(e -> {
            uploadedImages.remove(file);
            refreshImageList();
        });
        row.getChildren().addAll(thumb, name, removeButton);
        imageListBox.getChildren().add(row);
    }

    @FXML
    private void onSaveReport() {
        errorLabel.setText("");
        if (itemNameField.getText().isBlank()) {
            errorLabel.setText("Item name is required.");
            return;
        }
        if (categoryCombo.getValue() == null) {
            errorLabel.setText("Please select a category.");
            return;
        }
        if (descriptionArea.getText().isBlank()) {
            errorLabel.setText("Description is required.");
            return;
        }
        if (reporterNameField.getText().isBlank()) {
            errorLabel.setText("Reporter name is required.");
            return;
        }
        if (contactField.getText().isBlank()) {
            errorLabel.setText("Contact number is required.");
            return;
        }
        if (locationField.getText().isBlank()) {
            errorLabel.setText("Location is required.");
            return;
        }
        if (dateFoundPicker.getValue() == null) {
            errorLabel.setText("Date found is required.");
            return;
        }

        String dateFound = dateFoundPicker.getValue().format(DATE_FORMATTER);
        String imagePath = uploadedImages.isEmpty() ? "" : uploadedImages.get(0).toURI().toString();

        Item newItem = new Item(0,
                itemNameField.getText().trim(), Item.Status.LOST,
                descriptionArea.getText().trim(),
                dateFound,
                locationField.getText().trim(), imagePath);
        newItem.setCategory(categoryCombo.getValue());
        newItem.setReporterName(reporterNameField.getText().trim());
        newItem.setStudentId(studentIdField.getText().trim());
        newItem.setContactNumber(contactField.getText().trim());
        newItem.setDateFound(dateFound);

        if (onItemSaved != null) {
            onItemSaved.accept(newItem);
        } else {
            // Direct save via ItemStore (Figure 1 flow)
            ItemStore.getInstance().addItem(newItem);
        }
        navigateBack();
    }

    @FXML
    private void onCancel() {
        navigateBack();
    }

    @FXML
    private void onAddItem() {
    }

    @FXML
    private void onMenu() {
        navbar.toggle(menuButton);
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

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null)
                iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {
        }
    }
}
