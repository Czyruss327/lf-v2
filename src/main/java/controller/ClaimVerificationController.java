package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.campuslf.models.Claim;
import com.campuslf.models.ItemReport;
import com.campuslf.service.ActivityLogService;
import com.campuslf.service.ClaimService;
import com.campuslf.service.ItemService;
import model.Item;
import model.ItemStore;
import model.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ClaimVerificationController implements Initializable {

    private static final String NAME_PATTERN = "[A-Za-z .,]+";
    private static final String CONTACT_PATTERN = "09\\d{2}-\\d{3}-\\d{4}";
    private static final int MAX_COURSE_SECTION_LENGTH = 40;

    @FXML
    private ImageView logoImage;
    @FXML
    private Button addButton;
    @FXML
    private Button menuButton;
    @FXML
    private TextField claimNameField;
    @FXML
    private TextField studentIdField;
    @FXML
    private TextField contactField;
    @FXML
    private TextField courseSectionField;
    @FXML
    private TextField emailField;
    @FXML
    private DatePicker dateLostPicker;
    @FXML
    private DatePicker dateFoundPicker;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private CheckBox singleClaimantCheck;
    @FXML
    private CheckBox multipleClaimantsCheck;
    @FXML
    private VBox idListBox;
    @FXML
    private VBox proofListBox;
    @FXML
    private Label errorLabel;

    private Item item;
    private final ItemService itemService = new ItemService();
    private final ClaimService claimService = new ClaimService();
    private final ActivityLogService activityLogService = new ActivityLogService();
    private final List<File> proofImages = new ArrayList<>();
    private final List<File> idImages = new ArrayList<>();
    private NavbarHelper navbar;
    private ReportMenuHelper reportMenu;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        errorLabel.setText("");
        configureClaimantChecks();
        navbar = new NavbarHelper(() -> (Stage) claimNameField.getScene().getWindow());
        reportMenu = new ReportMenuHelper(() -> (Stage) claimNameField.getScene().getWindow());
    }

    public void setItem(Item item) {
        this.item = item;
        if (item == null) {
            return;
        }

        if (notBlank(item.getDate())) {
            dateLostPicker.getEditor().setText(item.getDate());
        }
        if (notBlank(item.getDateFound())) {
            dateFoundPicker.getEditor().setText(item.getDateFound());
        }
        if (notBlank(item.getColor())) {
            descriptionArea.setText(item.getColor());
        }
    }

    @FXML
    private void onUploadProof() {
        uploadFiles("Upload Proof of Claim", proofImages, proofListBox);
    }

    @FXML
    private void onUploadId() {
        uploadFiles("Upload ID", idImages, idListBox);
    }

    private void uploadFiles(String title, List<File> selectedFiles, VBox listBox) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images / PDF", "*.png", "*.jpg", "*.jpeg", "*.pdf"));
        Stage stage = (Stage) claimNameField.getScene().getWindow();
        List<File> files = chooser.showOpenMultipleDialog(stage);
        if (files == null)
            return;
        for (File file : files) {
            if (selectedFiles.size() >= 3)
                break;
            selectedFiles.add(file);
            listBox.getChildren().add(uploadRow(selectedFiles.size(), file.getName()));
        }
    }

    private HBox uploadRow(int index, String fileName) {
        HBox row = new HBox(8);
        row.getStyleClass().add("image-upload-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label name = new Label(index + ". " + fileName);
        name.getStyleClass().add("image-upload-name");
        name.setMaxWidth(260);
        name.setWrapText(true);
        row.getChildren().add(name);
        return row;
    }

    @FXML
    private void onConfirmClaim() {
        errorLabel.setText("");
        String claimantName = claimNameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String contactNumber = contactField.getText().trim();
        String courseSection = courseSectionField.getText().trim();
        boolean multipleClaimants = multipleClaimantsCheck.isSelected();

        if (claimantName.isBlank()) {
            errorLabel.setText("Name is required.");
            return;
        }
        if (!isValidName(claimantName)) {
            errorLabel.setText("Name can only contain letters, spaces, comma, and period.");
            return;
        }
        if (multipleClaimants && contactNumber.isBlank()) {
            errorLabel.setText("Contact number is required when there is more than one claimant.");
            return;
        }
        if (!contactNumber.isBlank() && !isValidContactNumber(contactNumber)) {
            errorLabel.setText("Contact number must follow this format: 09XX-XXX-XXXX");
            return;
        }
        if (courseSection.length() > MAX_COURSE_SECTION_LENGTH) {
            errorLabel.setText("Course and Section must not exceed " + MAX_COURSE_SECTION_LENGTH + " characters.");
            return;
        }
        if (dateLostPicker.getValue() == null && dateLostPicker.getEditor().getText().trim().isBlank()) {
            errorLabel.setText("Date Lost is required.");
            return;
        }
        if (proofImages.isEmpty()) {
            errorLabel.setText("Proof of Claim is required.");
            return;
        }
        if (!singleClaimantCheck.isSelected() && !multipleClaimantsCheck.isSelected()) {
            errorLabel.setText("Please select the number of claimants.");
            return;
        }

        if (item == null) {
            errorLabel.setText("Unable to submit claim. No item was selected.");
            return;
        }

        int adminId = resolveAdminId();
        Claim claim = new Claim();
        claim.setReportId(item.getId());
        claim.setAdminId(adminId);
        claim.setClaimantName(claimantName);
        claim.setClaimantStudentId(studentId.isBlank() ? null : studentId);
        claim.setClaimantContact(contactNumber.isBlank() ? null : contactNumber);
        claim.setCourseSection(courseSection.isBlank() ? null : courseSection);
        claim.setClaimStatus("Approved");
        claim.setDateClaimed(LocalDate.now());

        if (!claimService.submitClaim(claim)) {
            errorLabel.setText("Unable to save claim to Supabase.");
            return;
        }

        if (!itemService.markClaimed(item.getId())) {
            errorLabel.setText("Unable to update item status.");
            return;
        }
        activityLogService.logAction(
                adminId,
                "Claimed item: " + item.getName() + " by " + claimantName);
        ItemStore.getInstance().markAsClaimed(item, claimantName);

        showConfirmAndGoBack();
    }

    private int resolveAdminId() {
        int sessionAdminId = SessionManager.getInstance().getAdminId();
        if (sessionAdminId > 0) {
            return sessionAdminId;
        }

        if (item != null && item.getId() > 0) {
            ItemReport report = itemService.getItemById(item.getId());
            if (report != null && report.getAdminId() > 0) {
                return report.getAdminId();
            }
        }

        return 1;
    }

    private void showConfirmAndGoBack() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        String claimedAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy h:mm a"));
        alert.setTitle("Claim Confirmed");
        alert.setHeaderText(null);
        alert.setContentText(
                "Claim confirmed!\n\n" +
                        "- Audit log updated\n" +
                        "- Item status set to CLAIMED\n" +
                        "- Date/time claimed: " + claimedAt + "\n" +
                        "- Item removed from public dashboard\n\n" +
                        "Please hand over the physical item to the claimant.");
        alert.showAndWait();
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

    private void navigateBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) claimNameField.getScene().getWindow();
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
            Stage stage = (Stage) claimNameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle(title + " - PUPSRC Lost and Found");
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

    private boolean isValidName(String value) {
        return value.matches(NAME_PATTERN);
    }

    private boolean isValidContactNumber(String value) {
        return value.matches(CONTACT_PATTERN);
    }

    private void configureClaimantChecks() {
        singleClaimantCheck.selectedProperty().addListener((obs, oldValue, selected) -> {
            if (selected) {
                multipleClaimantsCheck.setSelected(false);
            } else if (!multipleClaimantsCheck.isSelected()) {
                singleClaimantCheck.setSelected(true);
            }
        });

        multipleClaimantsCheck.selectedProperty().addListener((obs, oldValue, selected) -> {
            if (selected) {
                singleClaimantCheck.setSelected(false);
            } else if (!singleClaimantCheck.isSelected()) {
                multipleClaimantsCheck.setSelected(true);
            }
        });
    }

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
