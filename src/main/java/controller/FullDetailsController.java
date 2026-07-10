package controller;

import com.campuslf.models.ReportStatus;
import com.campuslf.service.ActivityLogService;
import com.campuslf.service.ItemService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import mapper.ItemMapper;
import model.Item;
import model.SessionManager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FullDetailsController implements Initializable {

    @FXML
    private ImageView logoImage;
    @FXML
    private Button addButton;
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
    private TextField timeFoundField;
    @FXML
    private ComboBox<String> statusCombo;
    @FXML
    private Button claimBtn;
    @FXML
    private Button resolveBtn;
    @FXML
    private Button generatePdfBtn;

    private Item item;
    private NavbarHelper navbar;
    private ReportMenuHelper reportMenu;
    private final ItemService itemService = new ItemService();
    private final ActivityLogService activityLogService = new ActivityLogService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        categoryCombo.getItems().addAll(
                "Bags & Wallets", "Electronics", "IDs & Documents",
                "Clothing", "School Supplies", "Keys", "Accessories", "Others");
        statusCombo.getItems().addAll(ReportStatus.LOST, ReportStatus.FOUND, ReportStatus.CLAIMED, ReportStatus.RESOLVED);
        setAllReadOnly();
        navbar = new NavbarHelper(() -> (Stage) itemNameField.getScene().getWindow());
        reportMenu = new ReportMenuHelper(() -> (Stage) itemNameField.getScene().getWindow());
    }

    public void setItem(Item item) {
        this.item = item;
        itemNameField.setText(item.getName());
        descriptionArea.setText(item.getColor());
        locationField.setText(item.getLocation());
        dateFoundField.setText(item.getDate());
        timeFoundField.setText(valueOrDash(item.getTime()));
        statusCombo.setValue(item.getStatusLabel());
        categoryCombo.setValue(item.getCategory() != null ? item.getCategory() : "Others");
        reporterNameField.setText(item.getReporterName() != null ? item.getReporterName() : "");
        studentIdField.setText(item.getStudentId() != null ? item.getStudentId() : "");
        contactField.setText(item.getContactNumber() != null ? item.getContactNumber() : "");
        loadItemImage(item.getImagePath());
        updateActionButtonState();
    }

    public void setDashboardController(DashboardController dc) {
        // Kept for existing navigation wiring.
    }

    @FXML
    private void onClaim() {
        if (item == null || item.getStatus() != Item.Status.FOUND) {
            showAlert("Cannot Claim", "Only FOUND items can be claimed.");
            return;
        }

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
    private void onResolve() {
        if (item == null || item.getStatus() != Item.Status.LOST) {
            showAlert("Cannot Resolve", "Only LOST items can be marked as RESOLVED.");
            return;
        }

        ResolveChoice choice = showResolveDialog();
        if (choice == null) {
            return;
        }

        boolean resolved = choice.type() == ResolveType.EXISTING_FOUND
                ? itemService.markClaimed(choice.foundItem().getId()) && itemService.markResolved(item.getId())
                : itemService.markResolved(item.getId());

        if (!resolved) {
            showAlert("Resolve Failed", "Unable to update item status.");
            return;
        }

        item.setStatus(Item.Status.RESOLVED);
        statusCombo.setValue(item.getStatusLabel());
        updateActionButtonState();

        if (choice.type() == ResolveType.EXISTING_FOUND) {
            boolean claimSlipSaved = generateResolveClaimSlip(choice.foundItem());
            activityLogService.logAction(resolveAdminId(),
                    "Resolved lost item with found report: " + item.getName());
            showInfo("Resolved",
                    "Lost item report marked as RESOLVED and matching found item marked as CLAIMED.\n\n"
                            + "Claim slip: " + (claimSlipSaved ? "saved" : "not saved"));
        } else {
            activityLogService.logAction(resolveAdminId(),
                    "Resolved lost item outside system: " + item.getName());
            showInfo("Resolved", "Lost item report marked as RESOLVED outside the system database.");
        }
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

    private void setAllReadOnly() {
        itemNameField.setEditable(false);
        descriptionArea.setEditable(false);
        reporterNameField.setEditable(false);
        studentIdField.setEditable(false);
        contactField.setEditable(false);
        locationField.setEditable(false);
        dateFoundField.setEditable(false);
        timeFoundField.setEditable(false);
        categoryCombo.setDisable(true);
        statusCombo.setDisable(true);
    }

    @FXML
    private void onGeneratePdf() {
        if (item == null) {
            showAlert("No Item Selected", "Open an item before generating a PDF.");
            return;
        }

        if (!confirmPdfGeneration("Generate Blog Site PDF",
                "Do you want to generate a blog-site PDF for this item?")) {
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
            showInfo("PDF Generated", "The blog-site PDF has been saved.");
        } catch (IOException e) {
            showAlert("PDF Error", "Unable to generate the PDF file.");
        }
    }

    private void updateActionButtonState() {
        boolean canClaim = item != null && item.getStatus() == Item.Status.FOUND;
        claimBtn.setDisable(!canClaim);
        claimBtn.setText(canClaim ? "CLAIM" : item == null ? "CLAIM" : item.getStatusLabel());

        boolean canResolve = item != null && item.getStatus() == Item.Status.LOST;
        resolveBtn.setDisable(!canResolve);
        resolveBtn.setVisible(canResolve);
        resolveBtn.setManaged(canResolve);
    }

    private boolean generateResolveClaimSlip(Item foundItem) {
        if (!confirmPdfGeneration("Generate Claim Slip",
                "Generate a claim slip for this resolved lost item?")) {
            return false;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Claim Slip");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        LocalDate releasedDate = LocalDate.now();
        LocalTime releaseTime = LocalTime.now();
        chooser.setInitialFileName(buildClaimSlipFileName(item.getReporterName(), item.getName(), releasedDate, releaseTime));
        Stage stage = (Stage) itemNameField.getScene().getWindow();
        File output = chooser.showSaveDialog(stage);
        if (output == null) {
            return false;
        }

        String adminOfficer = valueOrDash(SessionManager.getInstance().getUsername());
        if ("-".equals(adminOfficer)) {
            adminOfficer = "Admin Account / Property Officer";
        }

        try {
            ClaimSlipPdfGenerator.write(output, ClaimSlipPdfGenerator.data(
                    0,
                    foundItem.getId(),
                    valueOrDefault(item.getReporterName(), "Lost Item Owner"),
                    valueOrDash(item.getStudentId()),
                    valueOrDash(item.getContactNumber()),
                    item.getName(),
                    buildResolvedClaimDescription(foundItem),
                    adminOfficer,
                    releasedDate,
                    releaseTime
            ));
            return true;
        } catch (IOException e) {
            showAlert("Claim Slip Error", "The lost item was resolved, but the claim slip PDF could not be generated.");
            return false;
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

    private String buildResolvedClaimDescription(Item foundItem) {
        return "Matched with found report #" + foundItem.getId()
                + " from " + valueOrDash(foundItem.getLocation())
                + ". " + valueOrDash(foundItem.getColor());
    }

    private ResolveChoice showResolveDialog() {
        List<Item> matches = findPossibleFoundMatches();

        Dialog<ResolveChoice> dialog = new Dialog<>();
        dialog.setTitle("Resolve Lost Item");
        dialog.setHeaderText(null);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        Button resolveButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        resolveButton.setText("RESOLVE");
        resolveButton.setDisable(matches.isEmpty());

        URL stylesheet = getClass().getResource("/css/styles.css");
        if (stylesheet != null) {
            dialog.getDialogPane().getStylesheets().add(stylesheet.toExternalForm());
        }
        dialog.getDialogPane().getStyleClass().add("resolve-dialog");

        ToggleGroup resolveMode = new ToggleGroup();
        RadioButton existingFoundOption = new RadioButton("Resolve with an existing found item");
        existingFoundOption.getStyleClass().add("form-radio");
        existingFoundOption.setToggleGroup(resolveMode);
        existingFoundOption.setDisable(matches.isEmpty());

        RadioButton externalReturnOption = new RadioButton("Resolve outside the system database");
        externalReturnOption.getStyleClass().add("form-radio");
        externalReturnOption.setToggleGroup(resolveMode);

        ListView<Item> matchList = new ListView<>(FXCollections.observableArrayList(matches));
        matchList.getStyleClass().add("resolve-match-list");
        matchList.setPrefHeight(matches.isEmpty() ? 86 : Math.min(260, 78 * matches.size() + 4));
        matchList.setPlaceholder(new Label("No matching found items are currently posted."));
        matchList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Item foundItem, boolean empty) {
                super.updateItem(foundItem, empty);
                if (empty || foundItem == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                setGraphic(buildMatchCell(foundItem));
                setText(null);
            }
        });

        if (!matches.isEmpty()) {
            existingFoundOption.setSelected(true);
            matchList.getSelectionModel().selectFirst();
        } else {
            externalReturnOption.setSelected(true);
        }

        matchList.disableProperty().bind(existingFoundOption.selectedProperty().not());

        resolveMode.selectedToggleProperty().addListener((obs, oldToggle, selectedToggle) -> {
            boolean external = selectedToggle == externalReturnOption;
            boolean canResolve = external || matchList.getSelectionModel().getSelectedItem() != null;
            resolveButton.setDisable(!canResolve);
        });
        matchList.getSelectionModel().selectedItemProperty().addListener((obs, oldItem, selectedItem) -> {
            boolean canResolve = externalReturnOption.isSelected() || selectedItem != null;
            resolveButton.setDisable(!canResolve);
        });

        VBox content = new VBox(14);
        content.setPadding(new Insets(8, 4, 4, 4));
        content.getChildren().addAll(
                sectionLabel("Possible Found Item Matches"),
                existingFoundOption,
                matchList,
                externalReturnOption,
                helperLabel("Use this when the finder returned the item directly and no found report exists."));
        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(button -> {
            if (button != ButtonType.OK) {
                return null;
            }
            if (externalReturnOption.isSelected()) {
                return new ResolveChoice(ResolveType.EXTERNAL_RETURN, null);
            }
            Item selected = matchList.getSelectionModel().getSelectedItem();
            return selected == null ? null : new ResolveChoice(ResolveType.EXISTING_FOUND, selected);
        });

        Stage stage = (Stage) itemNameField.getScene().getWindow();
        dialog.initOwner(stage);
        return dialog.showAndWait().orElse(null);
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-heading");
        return label;
    }

    private Label helperLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("resolve-helper");
        label.setWrapText(true);
        return label;
    }

    private HBox buildMatchCell(Item foundItem) {
        VBox details = new VBox(4);
        Label name = new Label(foundItem.getName());
        name.getStyleClass().add("resolve-match-title");
        Label meta = new Label(foundItem.getCategory() + " - " + valueOrDash(foundItem.getLocation()) + " - " + valueOrDash(foundItem.getDate()));
        meta.getStyleClass().add("card-meta");
        Label description = new Label(valueOrDash(foundItem.getColor()));
        description.getStyleClass().add("resolve-match-description");
        description.setWrapText(true);
        details.getChildren().addAll(name, meta, description);

        Label badge = new Label(ReportStatus.FOUND);
        badge.getStyleClass().addAll("badge", "badge-found");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox cell = new HBox(12, details, spacer, badge);
        cell.setPadding(new Insets(8, 6, 8, 6));
        return cell;
    }

    private List<Item> findPossibleFoundMatches() {
        return itemService.getVisibleItems(true).stream()
                .map(ItemMapper::toItem)
                .filter(candidate -> candidate.getStatus() == Item.Status.FOUND)
                .map(candidate -> new Match(candidate, matchScore(candidate)))
                .filter(match -> match.score() >= 2)
                .sorted(java.util.Comparator
                        .comparingInt(Match::score).reversed()
                        .thenComparing(match -> valueOrDash(match.item().getDate()), java.util.Comparator.reverseOrder())
                        .thenComparing(match -> match.item().getId()))
                .map(Match::item)
                .limit(8)
                .toList();
    }

    private int matchScore(Item candidate) {
        int score = 0;
        if (sameText(item.getCategory(), candidate.getCategory())) {
            score += 3;
        }

        java.util.Set<String> lostNameWords = searchableWords(item.getName());
        java.util.Set<String> foundNameWords = searchableWords(candidate.getName());
        java.util.Set<String> lostDescriptionWords = searchableWords(item.getColor());
        java.util.Set<String> foundDescriptionWords = searchableWords(candidate.getColor());

        score += overlapCount(lostNameWords, foundNameWords) * 3;
        score += overlapCount(lostNameWords, foundDescriptionWords) * 2;
        score += overlapCount(lostDescriptionWords, foundNameWords) * 2;
        score += overlapCount(lostDescriptionWords, foundDescriptionWords);

        if (sameText(item.getName(), candidate.getName())) {
            score += 5;
        }
        return score;
    }

    private java.util.Set<String> searchableWords(String value) {
        if (value == null || value.isBlank()) {
            return java.util.Set.of();
        }

        return java.util.regex.Pattern.compile("[^a-z0-9]+")
                .splitAsStream(value.toLowerCase())
                .filter(word -> word.length() > 2)
                .filter(word -> !java.util.Set.of(
                        "a", "an", "and", "at", "for", "from", "in", "is", "it", "of",
                        "on", "or", "the", "to", "with", "lost", "found", "item").contains(word))
                .collect(java.util.stream.Collectors.toSet());
    }

    private int overlapCount(java.util.Set<String> first, java.util.Set<String> second) {
        if (first.isEmpty() || second.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (String word : first) {
            if (second.contains(word)) {
                count++;
            }
        }
        return count;
    }

    private int resolveAdminId() {
        int adminId = SessionManager.getInstance().getAdminId();
        return adminId > 0 ? adminId : 1;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private OfficialReportPdfGenerator.ReportData buildOfficialReportData() {
        boolean foundReport = item.getStatus() == Item.Status.FOUND || item.getStatus() == Item.Status.CLAIMED;
        boolean anonymousFinder = foundReport && valueOrDash(item.getReporterName()).equals("-");
        return OfficialReportPdfGenerator.data(
                foundReport,
                item.getId(),
                item.getName(),
                item.getCategory(),
                item.getLocation(),
                null,
                item.getDate(),
                item.getTime(),
                item.getColor(),
                item.getReporterName(),
                "",
                item.getContactNumber(),
                anonymousFinder,
                "[" + valueOrDash(item.getName()) + " - Front View]",
                "[" + valueOrDash(item.getName()) + " - Alternate Angle]",
                List.of(valueOrDash(item.getImagePath()))
        );
    }

    private String buildPdfFileName() {
        boolean foundReport = item.getStatus() == Item.Status.FOUND || item.getStatus() == Item.Status.CLAIMED;
        boolean anonymousFinder = foundReport
                && (valueOrDash(item.getReporterName()).equals("-")
                || "Anonymous".equalsIgnoreCase(valueOrDash(item.getReporterName())));
        return PdfFileNameUtil.reportFileName(
                foundReport,
                anonymousFinder,
                item.getId(),
                item.getReporterName(),
                item.getName(),
                parseItemDate(item.getDate()));
    }

    private String buildClaimSlipFileName(String claimantName, String itemName, LocalDate releasedDate, LocalTime releaseTime) {
        return PdfFileNameUtil.claimSlipFileName(claimantName, itemName, releasedDate, releaseTime);
    }

    private LocalDate parseItemDate(String value) {
        if (value == null || value.isBlank() || "-".equals(value)) {
            return LocalDate.now();
        }
        for (String pattern : List.of("MMMM dd, yyyy", "MMMM d, yyyy", "MM/dd/yyyy", "yyyy-MM-dd")) {
            try {
                return LocalDate.parse(value, DateTimeFormatter.ofPattern(pattern));
            } catch (Exception ignored) {
            }
        }
        return LocalDate.now();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private boolean sameText(String first, String second) {
        return first != null && second != null && first.trim().equalsIgnoreCase(second.trim());
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

    private enum ResolveType {
        EXISTING_FOUND, EXTERNAL_RETURN
    }

    private record ResolveChoice(ResolveType type, Item foundItem) {
    }

    private record Match(Item item, int score) {
    }
}
