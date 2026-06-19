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
import com.campuslf.models.ItemReport;
import com.campuslf.service.ItemService;
import model.SessionManager;

import java.time.LocalDate;
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
    private ImageView reportFormIcon;
    @FXML
    private ImageView menuBarIcon;
    @FXML
    private Button addButton;
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

    private final ItemService itemService = new ItemService();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private final List<File> uploadedImages = new ArrayList<>();
    private Consumer<ItemReport> onItemSaved;
    private NavbarHelper navbar;

    private int getCategoryId(String category) {

        switch (category) {

            case "Electronics":
                return 1;

            case "Bags & Wallets":
                return 2;

            case "IDs & Documents":
                return 3;

            case "Clothing":
                return 4;

            default:
                return 5; // Others
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        if (loadImage(reportFormIcon, "/images/report-form.png")) {
            addButton.setText("");
        } else {
            addButton.setGraphic(null);
        }
        if (loadImage(menuBarIcon, "/images/menu-bar.png")) {
            menuButton.setText("");
        } else {
            menuButton.setGraphic(null);
        }
        errorLabel.setText("");
        categoryCombo.getItems().addAll(
                "Bags & Wallets", "Electronics", "IDs & Documents",
                "Clothing", "School Supplies", "Keys", "Accessories", "Others");
        navbar = new NavbarHelper(() -> (Stage) itemNameField.getScene().getWindow());
    }

    public void setOnItemSaved(Consumer<ItemReport> callback) {
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

        String imagePath =
                uploadedImages.isEmpty()
                        ? ""
                        : uploadedImages.get(0).toURI().toString();

        ItemReport report = new ItemReport();

        report.setCategoryId(
                getCategoryId(categoryCombo.getValue()));

        report.setItemName(
                itemNameField.getText().trim());

        report.setDescription(
                descriptionArea.getText().trim());

        report.setLocationFound(
                locationField.getText().trim());

        report.setDateReported(
                dateFoundPicker.getValue());

        report.setDatePosted(
                LocalDate.now());

        report.setFinderStudentId(
                studentIdField.getText().trim());

        report.setFinderContactNum(
                contactField.getText().trim());

        report.setImageUrl(
                imagePath);

        report.setReportStatus(
                "Unclaimed");

        boolean saved = itemService.addItem(report);

        if (!saved) {

            errorLabel.setText(
                    "Unable to save report.");

            return;
        }

        if (onItemSaved != null) {
            onItemSaved.accept(report);
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

    private boolean loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                iv.setImage(new Image(url.toExternalForm(), true));
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}