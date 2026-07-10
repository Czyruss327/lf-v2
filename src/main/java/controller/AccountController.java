package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
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
import com.campuslf.models.ItemReport;
import com.campuslf.models.ReportStatus;
import com.campuslf.service.ActivityLogService;
import com.campuslf.service.ItemService;
import model.ProfileStore;
import model.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private record BulkPdfOptions(String status, String groupBy, String category,
                                  LocalDate fromDate, LocalDate toDate, String location) {
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
    private final ItemService itemService = new ItemService();
    private static final DateTimeFormatter LOG_TIMESTAMP_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a");
    private static final DateTimeFormatter PDF_DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter PDF_MONTH_FMT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final List<String> IMAGE_EXTENSIONS = List.of("png", "jpg", "jpeg", "gif", "webp");

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
        if (!isImageFile(file)) {
            showAlert("Photo Error", "Only image files are allowed.");
            return;
        }

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
    private void onGenerateBulkPdf() {
        if (!SessionManager.getInstance().isAdmin()) {
            showAlert("Access Denied", "Only admins can generate bulk reports.");
            return;
        }

        BulkPdfOptions options = showBulkPdfDialog();
        if (options == null) {
            return;
        }

        List<ItemReport> reports = filterReports(options);
        if (reports.isEmpty()) {
            showAlert("No Reports", "No reports match the selected filters.");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Bulk Report PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        chooser.setInitialFileName(buildBulkPdfFileName(options));
        Stage stage = (Stage) menuButton.getScene().getWindow();
        File output = chooser.showSaveDialog(stage);
        if (output == null) {
            return;
        }

        try {
            BulkReportPdfGenerator.write(output,
                    "BULK LOST AND FOUND REPORT",
                    buildBulkSubtitle(options, reports.size()),
                    options.groupBy(),
                    groupReports(reports, options.groupBy()));
            activityLogService.logAction(
                    Math.max(1, SessionManager.getInstance().getAdminId()),
                    "Generated bulk PDF report: " + reports.size() + " items");
            buildHistory();
            showInfo("PDF Generated", "The bulk report PDF has been saved.");
        } catch (IOException e) {
            showAlert("PDF Error", "Unable to generate the bulk PDF.");
        }
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
                return activityLogService.getLogsByAdminId(resolveAdminId());
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
        VBox card = new VBox(8);
        card.getStyleClass().add("history-card");
        card.setPadding(new Insets(12, 16, 12, 16));

        Label id = new Label("LOG #" + entry.getLogId());
        id.getStyleClass().add("history-log-id");

        Label timestamp = new Label(formatLogTimestamp(entry));
        timestamp.getStyleClass().add("history-timestamp");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox metaRow = new HBox(10, id, spacer, timestamp);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label activity = new Label(valueOrDash(entry.getActivity()));
        activity.getStyleClass().add("history-card-title");
        activity.setWrapText(true);
        activity.setMaxWidth(Double.MAX_VALUE);

        Label admin = new Label("Admin ID: " + entry.getAdminId());
        admin.getStyleClass().add("history-card-sub");

        card.getChildren().addAll(metaRow, activity, admin);
        return card;
    }

    private String formatLogTimestamp(ActivityLog entry) {
        return entry.getTimestamp() == null ? " " : entry.getTimestamp().format(LOG_TIMESTAMP_FMT);
    }

    private BulkPdfOptions showBulkPdfDialog() {
        Dialog<BulkPdfOptions> dialog = new Dialog<>();
        dialog.setTitle("Generate Bulk PDF");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        Button generateButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        generateButton.setText("Generate PDF");

        URL stylesheet = getClass().getResource("/css/styles.css");
        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet.toExternalForm());
        }

        ComboBox<String> statusCombo = combo("All", List.of("All", ReportStatus.LOST, ReportStatus.FOUND, ReportStatus.CLAIMED, ReportStatus.RESOLVED));
        ComboBox<String> groupCombo = combo("Status", List.of("Status", "Week", "Month", "Category", "Location", "Date", "Time"));
        ComboBox<String> categoryCombo = combo("All", List.of("All", "Electronics", "Bags & Wallets", "IDs & Documents", "Clothing", "Others"));

        DatePicker fromPicker = new DatePicker();
        DatePicker toPicker = new DatePicker();
        fromPicker.getStyleClass().add("form-input");
        toPicker.getStyleClass().add("form-input");
        fromPicker.setMaxWidth(Double.MAX_VALUE);
        toPicker.setMaxWidth(Double.MAX_VALUE);

        TextField locationField = new TextField();
        locationField.setPromptText("Any location");
        locationField.getStyleClass().add("form-input");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.add(label("Item Type"), 0, 0);
        form.add(statusCombo, 1, 0);
        form.add(label("Group By"), 0, 1);
        form.add(groupCombo, 1, 1);
        form.add(label("Category"), 0, 2);
        form.add(categoryCombo, 1, 2);
        form.add(label("From Date"), 0, 3);
        form.add(fromPicker, 1, 3);
        form.add(label("To Date"), 0, 4);
        form.add(toPicker, 1, 4);
        form.add(label("Location"), 0, 5);
        form.add(locationField, 1, 5);

        ColumnConstraints labelCol = new ColumnConstraints();
        labelCol.setMinWidth(95);
        ColumnConstraints fieldCol = new ColumnConstraints();
        fieldCol.setHgrow(Priority.ALWAYS);
        form.getColumnConstraints().addAll(labelCol, fieldCol);

        VBox content = new VBox(14, sectionLabel("Bulk PDF Options"), form);
        content.setPadding(new Insets(8));
        content.setPrefWidth(420);
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            return new BulkPdfOptions(statusCombo.getValue(), groupCombo.getValue(), categoryCombo.getValue(),
                    fromPicker.getValue(), toPicker.getValue(), locationField.getText().trim());
        });

        Stage stage = (Stage) menuButton.getScene().getWindow();
        dialog.initOwner(stage);
        return dialog.showAndWait().orElse(null);
    }

    private List<ItemReport> filterReports(BulkPdfOptions options) {
        return itemService.getVisibleItems(true).stream()
                .filter(report -> "All".equals(options.status())
                        || ReportStatus.normalize(report.getReportStatus()).equals(options.status()))
                .filter(report -> "All".equals(options.category())
                        || categoryName(report.getCategoryId()).equals(options.category()))
                .filter(report -> {
                    LocalDate date = reportDate(report);
                    return (options.fromDate() == null || !date.isBefore(options.fromDate()))
                            && (options.toDate() == null || !date.isAfter(options.toDate()));
                })
                .filter(report -> options.location().isBlank()
                        || valueOrDash(report.getLocationFound()).toLowerCase().contains(options.location().toLowerCase()))
                .sorted(Comparator.comparing(this::reportDate).reversed()
                        .thenComparing(ItemReport::getReportId, Comparator.reverseOrder()))
                .toList();
    }

    private Map<String, List<ItemReport>> groupReports(List<ItemReport> reports, String groupBy) {
        Map<String, List<ItemReport>> grouped = new LinkedHashMap<>();
        for (ItemReport report : reports) {
            grouped.computeIfAbsent(groupLabel(report, groupBy), key -> new ArrayList<>()).add(report);
        }
        return grouped;
    }

    private String groupLabel(ItemReport report, String groupBy) {
        LocalDate date = reportDate(report);
        return switch (groupBy) {
            case "Week" -> "Week " + date.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()) + ", " + date.getYear();
            case "Month" -> date.format(PDF_MONTH_FMT);
            case "Category" -> categoryName(report.getCategoryId());
            case "Location" -> valueOrDash(report.getLocationFound());
            case "Date" -> date.format(PDF_DATE_FMT);
            case "Time" -> "No saved time";
            default -> ReportStatus.normalize(report.getReportStatus());
        };
    }

    private String buildBulkSubtitle(BulkPdfOptions options, int count) {
        List<String> parts = new ArrayList<>();
        parts.add(count + " item" + (count == 1 ? "" : "s"));
        parts.add("Type: " + options.status());
        parts.add("Group: " + options.groupBy());
        if (!"All".equals(options.category())) {
            parts.add("Category: " + options.category());
        }
        if (options.fromDate() != null || options.toDate() != null) {
            parts.add("Date: " + valueOrDash(formatDate(options.fromDate())) + " - " + valueOrDash(formatDate(options.toDate())));
        }
        if (!options.location().isBlank()) {
            parts.add("Location: " + options.location());
        }
        return String.join(" | ", parts);
    }

    private String buildBulkPdfFileName(BulkPdfOptions options) {
        return PdfFileNameUtil.bulkReportFileName(
                options.status(),
                options.groupBy(),
                options.category(),
                options.fromDate(),
                options.toDate(),
                options.location());
    }

    private LocalDate reportDate(ItemReport report) {
        if (report.getDateReported() != null) {
            return report.getDateReported();
        }
        if (report.getDatePosted() != null) {
            return report.getDatePosted();
        }
        return LocalDate.now();
    }

    private String categoryName(int categoryId) {
        return switch (categoryId) {
            case 1 -> "Electronics";
            case 2 -> "Bags & Wallets";
            case 3 -> "IDs & Documents";
            case 4 -> "Clothing";
            default -> "Others";
        };
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(PDF_DATE_FMT);
    }

    private ComboBox<String> combo(String value, List<String> values) {
        ComboBox<String> combo = new ComboBox<>();
        combo.getItems().addAll(values);
        combo.setValue(value);
        combo.getStyleClass().add("form-input");
        combo.setMaxWidth(Double.MAX_VALUE);
        return combo;
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-heading");
        return label;
    }

    // ── Helpers ───────────────────────────────────────────────

    private int resolveAdminId() {
        int adminId = SessionManager.getInstance().getAdminId();
        return adminId > 0 ? adminId : 1;
    }

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

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null)
                iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {
        }
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
