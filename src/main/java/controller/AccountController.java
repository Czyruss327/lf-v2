package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import com.campuslf.models.ActivityLog;
import com.campuslf.service.ActivityLogService;
import model.ProfileStore;
import model.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * AccountController
 * Shows profile picture (circle avatar with initials or custom photo),
 * admin name, Add Another Admin, Change Password, and a History list.
 * Profile photo is stored in ProfileStore and persists across sessions.
 */
public class AccountController implements Initializable {

    private record CropSelection(int x, int y, int size) {
    }

    @FXML
    private ImageView logoImage;
    @FXML
    private ImageView reportFormIcon;
    @FXML
    private ImageView menuBarIcon;
    @FXML
    private Button menuButton;
    @FXML
    private Button addButton;

    // Avatar
    @FXML
    private StackPane avatarPane;
    @FXML
    private Circle avatarCircle;
    @FXML
    private Label initialsLabel;
    @FXML
    private ImageView profileImageView;
    @FXML
    private VBox cameraOverlay;

    @FXML
    private Label roleLabel;
    @FXML
    private TextField adminNameField;

    // History
    @FXML
    private VBox historyList;
    @FXML
    private ScrollPane historyScrollPane;

    private NavbarHelper navbar;
    private ReportMenuHelper reportMenu;
    private final ActivityLogService activityLogService = new ActivityLogService();
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM / dd / yyyy");

    // ── Initialise ────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        loadImage(reportFormIcon, "/images/report-form.png");
        addButton.setText("");
        loadImage(menuBarIcon, "/images/menu-bar.png");
        menuButton.setText("");

        navbar = new NavbarHelper(() -> (Stage) menuButton.getScene().getWindow());
        reportMenu = new ReportMenuHelper(() -> (Stage) menuButton.getScene().getWindow());

        // Role / name
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        roleLabel.setText(isAdmin ? "Admin" : "Student");
        String username = SessionManager.getInstance().getUsername();
        adminNameField.setText(username != null ? username : "");

        // Initials
        String initial = (username != null && !username.isBlank())
                ? String.valueOf(username.charAt(0)).toUpperCase()
                : "?";
        initialsLabel.setText(initial);

        // Show stored photo if available
        String savedPath = ProfileStore.getInstance().getProfileImagePath();
        if (savedPath != null)
            applyPhoto(savedPath);

        // Hover: show/hide camera overlay
        avatarPane.setOnMouseEntered(e -> cameraOverlay.setVisible(true));
        avatarPane.setOnMouseExited(e -> cameraOverlay.setVisible(false));
        cameraOverlay.setVisible(false);

        // Admin-only actions
        boolean adminVisible = isAdmin;

        buildHistory();
    }

    // ── Avatar / photo ────────────────────────────────────────

    @FXML
    private void onChangePhoto() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
        Stage stage = (Stage) avatarPane.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file == null)
            return;

        String path = file.toURI().toString(); // file URI for Image
        try {
            Image original = new Image(path, false);
            CropSelection crop = chooseCrop(original);
            if (crop == null)
                return;

            ProfileStore.getInstance().setProfileImagePath(path);
            ProfileStore.getInstance().setProfileCrop(crop.x(), crop.y(), crop.size());
            showProfileImage(cropImage(original, crop));
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Photo Error", "Could not load that image.");
        }
    }

    private void applyPhoto(String uri) {
        try {
            Image original = new Image(uri, false);
            Image img = ProfileStore.getInstance().hasProfileCrop()
                    ? cropImage(original, new CropSelection(
                            ProfileStore.getInstance().getProfileCropX(),
                            ProfileStore.getInstance().getProfileCropY(),
                            ProfileStore.getInstance().getProfileCropSize()))
                    : squareCrop(original);

            showProfileImage(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProfileImage(Image img) {
        Circle clip = new Circle(60, 60, 60);
        profileImageView.setClip(clip);

        profileImageView.setImage(img);
        profileImageView.setVisible(true);
        initialsLabel.setVisible(false);
        avatarCircle.setVisible(false);
    }

    private Image squareCrop(Image src) {
        double d = Math.min(src.getWidth(), src.getHeight());
        int size = (int) d;
        int x = (int) ((src.getWidth() - d) / 2);
        int y = (int) ((src.getHeight() - d) / 2);

        PixelReader reader = src.getPixelReader();
        return new WritableImage(reader, x, y, size, size);
    }

    private Image cropImage(Image src, CropSelection crop) {
        int maxSize = (int) Math.min(src.getWidth(), src.getHeight());
        int size = Math.max(1, Math.min(crop.size(), maxSize));
        int x = clamp(crop.x(), 0, (int) src.getWidth() - size);
        int y = clamp(crop.y(), 0, (int) src.getHeight() - size);

        PixelReader reader = src.getPixelReader();
        return new WritableImage(reader, x, y, size, size);
    }

    private CropSelection chooseCrop(Image src) {
        Dialog<CropSelection> dialog = new Dialog<>();
        dialog.setTitle("Adjust Profile Photo");
        dialog.setHeaderText("Drag the square to choose the profile picture crop.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        double maxPreview = 360;
        double scale = Math.min(maxPreview / src.getWidth(), maxPreview / src.getHeight());
        if (scale <= 0)
            return null;

        double previewWidth = src.getWidth() * scale;
        double previewHeight = src.getHeight() * scale;
        double maxCropSize = Math.min(previewWidth, previewHeight);
        double cropSize = maxCropSize;

        ImageView preview = new ImageView(src);
        preview.setFitWidth(previewWidth);
        preview.setFitHeight(previewHeight);
        preview.setPreserveRatio(false);

        Rectangle cropBox = new Rectangle(cropSize, cropSize);
        cropBox.setX((previewWidth - cropSize) / 2);
        cropBox.setY((previewHeight - cropSize) / 2);
        cropBox.setFill(javafx.scene.paint.Color.TRANSPARENT);
        cropBox.setStroke(javafx.scene.paint.Color.WHITE);
        cropBox.setStrokeWidth(3);
        cropBox.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 8, 0, 0, 0);");

        Rectangle resizeHandle = new Rectangle(16, 16);
        resizeHandle.setArcWidth(4);
        resizeHandle.setArcHeight(4);
        resizeHandle.setFill(javafx.scene.paint.Color.WHITE);
        resizeHandle.setStroke(javafx.scene.paint.Color.web("#5c1717"));
        resizeHandle.setStrokeWidth(2);
        positionResizeHandle(cropBox, resizeHandle);

        Pane cropPane = new Pane(preview, cropBox, resizeHandle);
        cropPane.setPrefSize(previewWidth, previewHeight);
        cropPane.setMinSize(previewWidth, previewHeight);
        cropPane.setMaxSize(previewWidth, previewHeight);

        final double[] dragOffset = new double[2];
        cropBox.setOnMousePressed(e -> {
            dragOffset[0] = e.getX() - cropBox.getX();
            dragOffset[1] = e.getY() - cropBox.getY();
        });
        cropBox.setOnMouseDragged(e -> {
            cropBox.setX(clamp(e.getX() - dragOffset[0], 0, previewWidth - cropBox.getWidth()));
            cropBox.setY(clamp(e.getY() - dragOffset[1], 0, previewHeight - cropBox.getHeight()));
            positionResizeHandle(cropBox, resizeHandle);
        });

        double minCropSize = Math.min(80, maxCropSize);
        Slider sizeSlider = new Slider(minCropSize, maxCropSize, cropSize);
        sizeSlider.setShowTickMarks(false);
        sizeSlider.setShowTickLabels(false);
        sizeSlider.setMaxWidth(previewWidth);
        sizeSlider.valueProperty().addListener((obs, oldValue, newValue) -> resizeCropBox(cropBox, resizeHandle,
                newValue.doubleValue(), previewWidth, previewHeight));

        resizeHandle.setOnMouseDragged(e -> {
            double newSize = Math.max(e.getX() - cropBox.getX(), e.getY() - cropBox.getY());
            newSize = clamp(newSize, sizeSlider.getMin(), maxCropSize);
            sizeSlider.setValue(newSize);
        });

        Label sizeLabel = new Label("Crop size");
        sizeLabel.setStyle("-fx-font-weight: bold;");

        VBox content = new VBox(12, cropPane, sizeLabel, sizeSlider);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(8));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK)
                return null;

            double originalScale = src.getWidth() / previewWidth;
            int x = (int) Math.round(cropBox.getX() * originalScale);
            int y = (int) Math.round(cropBox.getY() * originalScale);
            int size = (int) Math.round(cropBox.getWidth() * originalScale);
            return new CropSelection(x, y, size);
        });

        Optional<CropSelection> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private void resizeCropBox(Rectangle cropBox, Rectangle resizeHandle,
            double size, double previewWidth, double previewHeight) {
        double oldCenterX = cropBox.getX() + cropBox.getWidth() / 2;
        double oldCenterY = cropBox.getY() + cropBox.getHeight() / 2;

        cropBox.setWidth(size);
        cropBox.setHeight(size);
        cropBox.setX(clamp(oldCenterX - size / 2, 0, previewWidth - size));
        cropBox.setY(clamp(oldCenterY - size / 2, 0, previewHeight - size));
        positionResizeHandle(cropBox, resizeHandle);
    }

    private void positionResizeHandle(Rectangle cropBox, Rectangle resizeHandle) {
        resizeHandle.setX(cropBox.getX() + cropBox.getWidth() - resizeHandle.getWidth() / 2);
        resizeHandle.setY(cropBox.getY() + cropBox.getHeight() - resizeHandle.getHeight() / 2);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }

    // ── Buttons ───────────────────────────────────────────────

    @FXML
    private void onAddAdmin() {
        if (!SessionManager.getInstance().isAdmin()) {
            showAlert("Access Denied", "Only admins can add new admin accounts.");
            return;
        }
        navigateTo("/fxml/CreateAdminAccount.fxml", "Create Admin - PUPSRC Lost and Found");
    }

    @FXML
    private void onChangePassword() {
        // Record the event then show a placeholder dialog
        // (password-change UI can be a separate FXML later)
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Change Password");
        dlg.setHeaderText(null);
        dlg.setContentText(
                "Password change feature coming soon.\n\nFor now, manage passwords through the Create Admin Account screen.");
        dlg.showAndWait();
        activityLogService.logAction(
                Math.max(1, SessionManager.getInstance().getAdminId()),
                "Opened change password");
        buildHistory();
    }

    @FXML
    private void onAddItem() {
        if (!SessionManager.getInstance().isAdmin()) {
            showAlert("Access Denied", "Only admins can post new items.");
            return;
        }
        reportMenu.toggle(addButton);
    }

    @FXML
    private void onMenu() {
        navbar.toggle(menuButton);
    }

    // ── History ───────────────────────────────────────────────

    private void buildHistory() {
        historyList.getChildren().clear();
        historyList.getChildren().add(new Label("Loading history..."));

        Task<List<ActivityLog>> task = new Task<>() {
            @Override
            protected List<ActivityLog> call() {
                return activityLogService.getAllLogs();
            }
        };

        task.setOnSucceeded(event -> {
            historyList.getChildren().clear();
            List<ActivityLog> logs = task.getValue();
            if (logs.isEmpty()) {
                historyList.getChildren().add(new Label("No activity yet."));
                return;
            }

            for (ActivityLog log : logs) {
                historyList.getChildren().add(buildHistoryCard(log));
            }
        });

        task.setOnFailed(event -> {
            historyList.getChildren().clear();
            historyList.getChildren().add(new Label("Unable to load history."));
        });

        Thread historyThread = new Thread(task, "activity-log-loader");
        historyThread.setDaemon(true);
        historyThread.start();
    }

    private VBox buildHistoryCard(ActivityLog entry) {
        VBox card = new VBox(2);
        card.getStyleClass().add("history-card");
        card.setPadding(new Insets(12, 16, 12, 16));

        Label title = new Label(entry.getActivity());
        title.getStyleClass().add("history-card-title");

        String timeText = entry.getTimestamp() == null ? "" : entry.getTimestamp().format(TIME_FMT);
        Label time = new Label(timeText);
        time.getStyleClass().add("history-card-sub");

        String dateText = entry.getTimestamp() == null ? "" : entry.getTimestamp().format(DATE_FMT);
        Label date = new Label(dateText);
        date.getStyleClass().add("history-card-sub");

        card.getChildren().addAll(title, time, date);
        return card;
    }

    // ── Helpers ───────────────────────────────────────────────

    private void navigateTo(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            if ("/fxml/ReportForm.fxml".equals(fxml)) {
                ReportFormController ctrl = loader.getController();
                ctrl.setFoundReportMode();
            }
            Stage stage = (Stage) menuButton.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
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
