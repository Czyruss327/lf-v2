package controller;

import com.campuslf.models.ItemReport;
import com.campuslf.models.ReportStatus;
import com.campuslf.service.ActivityLogService;
import com.campuslf.service.ItemService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

/**
 * Handles the lost/found item report form and blog-site PDF export.
 */
public class ReportFormController implements Initializable {

    private static final int MAX_ITEM_NAME_LENGTH = 60;
    private static final int MAX_DESCRIPTION_LENGTH = 500;
    private static final String NAME_PATTERN = "[A-Za-z .,]+";
    private static final String ALPHANUMERIC_CONTENT_PATTERN = ".*[A-Za-z0-9].*";
    private static final String STUDENT_ID_PATTERN = "\\d{4}-\\d{5}-SR-0";
    private static final String CONTACT_PATTERN = "09\\d{2}-\\d{3}-\\d{4}";
    private static final String TIME_PATTERN = "([01]\\d|2[0-3]):[0-5]\\d";
    private static final List<String> IMAGE_EXTENSIONS = List.of("png", "jpg", "jpeg", "gif", "webp");

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
    private Label formTitleLabel;
    @FXML
    private Label itemSectionLabel;
    @FXML
    private Label itemNameLabel;
    @FXML
    private Label reporterSectionLabel;
    @FXML
    private Label locationLabel;
    @FXML
    private Label dateLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label nameRequiredStar;
    @FXML
    private Label contactRequiredStar;
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
    private TextField emailField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField foundLocationMirrorField;
    @FXML
    private DatePicker dateFoundPicker;
    @FXML
    private DatePicker foundDateMirrorPicker;
    @FXML
    private TextField timeField;
    @FXML
    private TextField lostTimeField;
    @FXML
    private VBox finderOptionsBox;
    @FXML
    private VBox reporterFieldsBox;
    @FXML
    private VBox emailBox;
    @FXML
    private VBox studentIdBox;
    @FXML
    private VBox lostLocationBox;
    @FXML
    private VBox lostDateBox;
    @FXML
    private VBox lostTimeBox;
    @FXML
    private VBox foundLocationBox;
    @FXML
    private VBox foundDateBox;
    @FXML
    private VBox timeBox;
    @FXML
    private CheckBox recordFinderDetailsCheck;
    @FXML
    private CheckBox anonymousFinderCheck;
    @FXML
    private StackPane modalOverlay;
    @FXML
    private Label errorLabel;

    private final ItemService itemService = new ItemService();
    private final ActivityLogService activityLogService = new ActivityLogService();
    private final List<File> uploadedImages = new ArrayList<>();
    private NavbarHelper navbar;
    private ReportMenuHelper reportMenu;
    private boolean foundReportMode;
    private ItemReport savedReport;
    private String savedReporterName = "";
    private String savedEmailAddress = "";
    private String savedPublicDescription = "";
    private String savedTime = "";
    private boolean savedAnonymousFinder;

    private int getCategoryId(String category) {
        category = category.toUpperCase();
        return switch (category) {
            case "ELECTRONICS" -> 1;
            case "BAGS & WALLETS" -> 2;
            case "IDS & DOCUMENTS" -> 3;
            case "CLOTHING" -> 4;
            default -> 5;
        };
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
        reportMenu = new ReportMenuHelper(() -> (Stage) itemNameField.getScene().getWindow());

        installLengthLimit(itemNameField, MAX_ITEM_NAME_LENGTH);
        installLengthLimit(descriptionArea, MAX_DESCRIPTION_LENGTH);
        installContactFormatter(contactField);
        installTimeFormatter(timeField);
        installTimeFormatter(lostTimeField);

        foundLocationMirrorField.textProperty().bindBidirectional(locationField.textProperty());
        foundDateMirrorPicker.valueProperty().bindBidirectional(dateFoundPicker.valueProperty());
        lostTimeField.textProperty().bindBidirectional(timeField.textProperty());
        recordFinderDetailsCheck.selectedProperty().addListener((obs, oldValue, selected) -> {
            if (selected) {
                anonymousFinderCheck.setSelected(false);
            }
            updateFinderFields();
        });
        anonymousFinderCheck.selectedProperty().addListener((obs, oldValue, selected) -> {
            if (selected) {
                recordFinderDetailsCheck.setSelected(false);
            }
            updateFinderFields();
        });

        applyReportMode();
    }

    public void setFoundReportMode() {
        foundReportMode = true;
        applyReportMode();
    }

    public void setLostReportMode() {
        foundReportMode = false;
        applyReportMode();
    }

    @FXML
    private void onUploadImages() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Item Photos");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (uploadedImages.size() >= 5) {
                break;
            }
            if (!isImageFile(file)) {
                errorLabel.setText(file.getName() + " is not a supported image file.");
                continue;
            }
            uploadedImages.add(file);
        }
        refreshImageList();
    }

    @FXML
    private void onSaveReport() {
        errorLabel.setText("");
        String itemName = itemNameField.getText().trim();
        String description = descriptionArea.getText().trim();
        String reporterName = reporterNameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String contactNumber = contactField.getText().trim();
        String location = locationField.getText().trim();
        String time = timeField.getText().trim();
        String emailAddress = emailField.getText().trim();

        if (!validateReport(itemName, description, reporterName, studentId, contactNumber, location, time)) {
            return;
        }

        String imagePath = uploadedImages.isEmpty() ? "" : uploadedImages.get(0).toURI().toString();
        ItemReport report = new ItemReport();
        report.setAdminId(Math.max(1, SessionManager.getInstance().getAdminId()));
        report.setCategoryId(getCategoryId(categoryCombo.getValue()));
        report.setItemName(itemName);
        report.setDescription(buildStoredDescription(reporterName, description));
        report.setLocationFound(location);
        report.setDateReported(dateFoundPicker.getValue());
        report.setDatePosted(LocalDate.now());
        report.setFinderStudentId(studentId.isBlank() ? null : studentId);
        report.setFinderContactNum(contactNumber.isBlank() ? null : contactNumber);
        report.setImageUrl(imagePath);
        report.setReportStatus(foundReportMode ? ReportStatus.FOUND : ReportStatus.LOST);

        boolean saved = itemService.addItem(report);
        if (!saved) {
            errorLabel.setText("Unable to save report.");
            return;
        }

        activityLogService.logAction(
                Math.max(1, SessionManager.getInstance().getAdminId()),
                "Posted new " + report.getReportStatus().toLowerCase() + " item: " + itemName);

        savedReport = report;
        savedReporterName = reporterName.isBlank() ? "Anonymous" : reporterName;
        savedEmailAddress = emailAddress;
        savedPublicDescription = description;
        savedTime = time;
        savedAnonymousFinder = foundReportMode && anonymousFinderCheck.isSelected();
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
    }

    @FXML
    private void onGeneratePdf() {
        if (savedReport == null) {
            errorLabel.setText("Save the report before generating a PDF.");
            modalOverlay.setVisible(false);
            modalOverlay.setManaged(false);
            return;
        }

        if (!confirmPdfGeneration("Generate Blog Site PDF",
                "Do you want to generate a blog-site PDF for this report?")) {
            navigateBack();
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Blog Site PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName(buildPdfFileName());
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        File output = chooser.showSaveDialog(stage);
        if (output == null) {
            return;
        }

        try {
            OfficialReportPdfGenerator.write(output, buildOfficialReportData());
            showAlert("PDF Generated", "The blog-site PDF has been saved.");
            navigateBack();
        } catch (IOException e) {
            showAlert("PDF Error", "Unable to generate the PDF file.");
        }
    }

    private boolean confirmPdfGeneration(String title, String message) {
        ButtonType generate = new ButtonType("Generate PDF", ButtonBar.ButtonData.OK_DONE);
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, message, generate, close);
        confirm.setTitle(title);
        confirm.setHeaderText(null);
        return confirm.showAndWait().orElse(close) == generate;
    }

    @FXML
    private void onCloseSavedModal() {
        navigateBack();
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

    private void applyReportMode() {
        if (formTitleLabel == null) {
            return;
        }

        formTitleLabel.setText(foundReportMode ? "FOUND ITEM REPORT" : "LOST ITEM REPORT");
        itemSectionLabel.setText(foundReportMode ? "Found Item Details" : "Lost Item Details");
        itemNameLabel.setText(foundReportMode ? "Found Item Name" : "Lost Item Name");
        locationLabel.setText(foundReportMode ? "Location Found" : "Location Lost");
        dateLabel.setText(foundReportMode ? "Date Found" : "Date Lost");
        timeLabel.setText(foundReportMode ? "Time Found" : "Time Lost");
        reporterSectionLabel.setText("Reporter Information");

        setVisibleManaged(finderOptionsBox, foundReportMode);
        setVisibleManaged(emailBox, !foundReportMode);
        setVisibleManaged(studentIdBox, !foundReportMode);
        setVisibleManaged(lostLocationBox, !foundReportMode);
        setVisibleManaged(lostDateBox, !foundReportMode);
        setVisibleManaged(lostTimeBox, !foundReportMode);
        setVisibleManaged(timeBox, foundReportMode);
        setVisibleManaged(foundLocationBox, foundReportMode);
        setVisibleManaged(foundDateBox, foundReportMode);

        if (foundReportMode) {
            recordFinderDetailsCheck.setSelected(false);
            anonymousFinderCheck.setSelected(false);
        }
        updateFinderFields();
    }

    private void updateFinderFields() {
        boolean showFinderFields = !foundReportMode || recordFinderDetailsCheck.isSelected();
        boolean finderFieldsRequired = !foundReportMode || recordFinderDetailsCheck.isSelected();
        setVisibleManaged(reporterFieldsBox, showFinderFields);
        nameRequiredStar.setVisible(finderFieldsRequired);
        nameRequiredStar.setManaged(finderFieldsRequired);
        contactRequiredStar.setVisible(finderFieldsRequired);
        contactRequiredStar.setManaged(finderFieldsRequired);

        if (foundReportMode && anonymousFinderCheck.isSelected()) {
            reporterNameField.clear();
            contactField.clear();
        }
    }

    private boolean validateReport(String itemName, String description, String reporterName,
                                   String studentId, String contactNumber, String location, String time) {
        if (itemName.isBlank()) {
            errorLabel.setText("Item name is required.");
            return false;
        }
        if (itemName.length() > MAX_ITEM_NAME_LENGTH) {
            errorLabel.setText("Item name must not exceed " + MAX_ITEM_NAME_LENGTH + " characters.");
            return false;
        }
        if (!hasAlphanumericContent(itemName)) {
            errorLabel.setText("Invalid input.");
            return false;
        }
        if (categoryCombo.getValue() == null) {
            errorLabel.setText("Please select a category.");
            return false;
        }
        if (description.isBlank()) {
            errorLabel.setText("Description is required.");
            return false;
        }
        if (!hasAlphanumericContent(description)) {
            errorLabel.setText("Invalid input.");
            return false;
        }
        if (description.length() > MAX_DESCRIPTION_LENGTH) {
            errorLabel.setText("Description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
            return false;
        }
        if (location.isBlank()) {
            errorLabel.setText("Location is required.");
            return false;
        }
        if (!hasAlphanumericContent(location)) {
            errorLabel.setText("Invalid input.");
            return false;
        }
        if (dateFoundPicker.getValue() == null) {
            errorLabel.setText((foundReportMode ? "Date found" : "Date lost") + " is required.");
            return false;
        }
        if (time.isBlank()) {
            errorLabel.setText((foundReportMode ? "Time found" : "Time lost") + " is required.");
            return false;
        }
        if (!isValidTime(time)) {
            errorLabel.setText("Time must follow 24-hour format: XX:XX");
            return false;
        }

        boolean finderDetailsRequired = !foundReportMode || recordFinderDetailsCheck.isSelected();
        boolean finderDetailsVisible = reporterFieldsBox.isVisible();
        if (finderDetailsRequired && reporterName.isBlank()) {
            errorLabel.setText("Reporter name is required.");
            return false;
        }
        if (finderDetailsVisible && !reporterName.isBlank() && !isValidName(reporterName)) {
            errorLabel.setText("Name can only contain letters, spaces, comma, and period.");
            return false;
        }
        if (finderDetailsVisible && !reporterName.isBlank() && !hasLetterContent(reporterName)) {
            errorLabel.setText("Invalid input.");
            return false;
        }
        if (!studentId.isBlank() && !isValidStudentId(studentId)) {
            errorLabel.setText("Student ID must follow this format: 2023-00123-SR-0");
            return false;
        }
        if (finderDetailsRequired && contactNumber.isBlank()) {
            errorLabel.setText("Contact number is required.");
            return false;
        }
        if (!contactNumber.isBlank() && !isValidContactNumber(contactNumber)) {
            errorLabel.setText("Contact number must follow this format: 09XX-XXX-XXXX");
            return false;
        }
        return true;
    }

    private String buildStoredDescription(String reporterName, String description) {
        String name = reporterName.isBlank() ? "Anonymous" : reporterName;
        return "Finder: " + name
                + System.lineSeparator()
                + "Report Time: " + timeField.getText().trim()
                + System.lineSeparator()
                + description;
    }

    private OfficialReportPdfGenerator.ReportData buildOfficialReportData() {
        String itemName = savedReport.getItemName();
        return OfficialReportPdfGenerator.data(
                foundReportMode,
                savedReport.getReportId(),
                itemName,
                categoryCombo.getValue(),
                savedReport.getLocationFound(),
                savedReport.getDateReported(),
                null,
                savedTime,
                savedPublicDescription,
                savedReporterName,
                savedEmailAddress,
                savedReport.getFinderContactNum(),
                savedAnonymousFinder,
                "[" + itemName + " - Front View]",
                "[" + itemName + " - Alternate Angle]",
                uploadedImages.stream()
                        .limit(2)
                        .map(file -> file.toURI().toString())
                        .toList()
        );
    }

    private String buildPdfFileName() {
        return PdfFileNameUtil.reportFileName(
                foundReportMode,
                savedAnonymousFinder,
                savedReport.getReportId(),
                savedReporterName,
                savedReport.getItemName(),
                savedReport.getDatePosted());
    }

    private void installLengthLimit(TextInputControl control, int maxLength) {
        control.setTextFormatter(new TextFormatter<String>(change ->
                change.getControlNewText().length() <= maxLength ? change : null));
    }

    private void installContactFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(contactChangeFilter()));
    }

    private UnaryOperator<TextFormatter.Change> contactChangeFilter() {
        return change -> isValidPartialContact(change.getControlNewText()) ? change : null;
    }

    private boolean isValidPartialContact(String value) {
        if (value.length() > 13) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if ((i == 4 || i == 8) && ch != '-') {
                return false;
            }
            if (i != 4 && i != 8 && !Character.isDigit(ch)) {
                return false;
            }
        }
        return value.length() < 1 || value.charAt(0) == '0'
                && (value.length() < 2 || value.charAt(1) == '9');
    }

    private void installTimeFormatter(TextField field) {
        field.setTextFormatter(new TextFormatter<String>(timeChangeFilter()));
    }

    private UnaryOperator<TextFormatter.Change> timeChangeFilter() {
        return change -> isValidPartialTime(change.getControlNewText()) ? change : null;
    }

    private boolean isValidPartialTime(String value) {
        if (value.length() > 5) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (i == 2) {
                if (ch != ':') {
                    return false;
                }
            } else if (!Character.isDigit(ch)) {
                return false;
            }
        }
        if (value.length() >= 1 && value.charAt(0) > '2') {
            return false;
        }
        if (value.length() >= 2 && value.charAt(0) == '2' && value.charAt(1) > '3') {
            return false;
        }
        return value.length() < 4 || value.charAt(3) <= '5';
    }

    private void refreshImageList() {
        imageListBox.getChildren().clear();
        for (int i = 0; i < uploadedImages.size(); i++) {
            addImageRow(uploadedImages.get(i), i);
        }
    }

    private void addImageRow(File file, int index) {
        HBox row = new HBox(12);
        row.getStyleClass().add("image-upload-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        ImageView thumb = new ImageView(new Image(file.toURI().toString(), 44, 44, true, true));
        thumb.setFitWidth(44);
        thumb.setFitHeight(44);
        Label name = new Label("Image " + (index + 1));
        name.getStyleClass().add("image-upload-name");
        HBox.setHgrow(name, javafx.scene.layout.Priority.ALWAYS);
        Button removeButton = new Button("X");
        removeButton.getStyleClass().add("image-remove-btn");
        removeButton.setOnAction(e -> {
            uploadedImages.remove(file);
            refreshImageList();
        });
        row.getChildren().addAll(thumb, name, removeButton);
        imageListBox.getChildren().add(row);
    }

    private void setVisibleManaged(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
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

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isValidName(String value) {
        return value.matches(NAME_PATTERN);
    }

    private boolean hasAlphanumericContent(String value) {
        return value != null && value.matches(ALPHANUMERIC_CONTENT_PATTERN);
    }

    private boolean hasLetterContent(String value) {
        return value != null && value.chars().anyMatch(Character::isLetter);
    }

    private boolean isValidStudentId(String value) {
        return value.matches(STUDENT_ID_PATTERN);
    }

    private boolean isValidContactNumber(String value) {
        return value.matches(CONTACT_PATTERN);
    }

    private boolean isValidTime(String value) {
        return value.matches(TIME_PATTERN);
    }

    private boolean isImageFile(File file) {
        if (file == null || !file.isFile()) {
            return false;
        }
        String name = file.getName();
        int dot = name == null ? -1 : name.lastIndexOf('.');
        return dot >= 0
                && dot < name.length() - 1
                && IMAGE_EXTENSIONS.contains(name.substring(dot + 1).toLowerCase());
    }
}
