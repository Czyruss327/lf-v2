package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Admin;
import model.HistoryEntry;

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class AccountController implements Initializable {

    // ── root of this page (StackPane) – drawer is injected here ──
    @FXML private StackPane      rootStack;

    // ── fx:include injects the NavbarController automatically ──
    @FXML private NavbarController navbarController;

    @FXML private Circle    avatarCircle;
    @FXML private TextField adminNameField;
    @FXML private VBox      historyContainer;

    private Admin currentAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Wire the drawer into the root StackPane so it covers the full window
        javafx.application.Platform.runLater(() -> {
            navbarController.attachDrawerTo(rootStack);
            navbarController.setNavigationHandler(this::onNavigate);
        });
    }

    // ─── Called by loading controller ────────────────────────────────────────

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        populateView();
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    private void onNavigate(String page) {
        try {
            String fxml = switch (page) {
                case "Dashboard"         -> "/fxml/Dashboard.fxml";
                case "ReportForm"        -> "/fxml/ReportForm.fxml";
                case "ClaimVerification" -> "/fxml/ClaimVerification.fxml";
                default -> null;
            };
            if (fxml == null) return;

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Stage stage = (Stage) rootStack.getScene().getWindow();
            boolean wasMaximized = stage.isMaximized();
            stage.setScene(new Scene(root));
            if (wasMaximized) stage.setMaximized(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── View population ─────────────────────────────────────────────────────

    private void populateView() {
        if (currentAdmin == null) return;
        adminNameField.setText(currentAdmin.getUsername());
        buildHistoryList();
    }

    private void buildHistoryList() {
        historyContainer.getChildren().clear();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("h:mm a");
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MM / dd / yyyy");

        for (HistoryEntry entry : currentAdmin.getHistory()) {
            VBox row = new VBox(1);
            row.getStyleClass().add("history-entry");

            Label typeLabel = new Label(entry.getAction());
            typeLabel.getStyleClass().add("history-entry-type");

            Label timeLabel = new Label(entry.getTimestamp().format(timeFmt));
            timeLabel.getStyleClass().add("history-entry-time");

            Label dateLabel = new Label(entry.getTimestamp().format(dateFmt));
            dateLabel.getStyleClass().add("history-entry-time");

            row.getChildren().addAll(typeLabel, timeLabel, dateLabel);
            historyContainer.getChildren().add(row);
        }
    }

    // ─── Button handlers ─────────────────────────────────────────────────────

    @FXML
    private void handleAddAnotherAdmin() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/AddAdmin.fxml"));
            Parent root = loader.load();

            AddAdminController c = loader.getController();
            c.setSourceAdmin(currentAdmin);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Add Another Admin");
            dialog.setResizable(true);
            dialog.setMinWidth(640);
            dialog.setMinHeight(480);
            dialog.setScene(new Scene(root));
            dialog.centerOnScreen();
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleChangePassword() {
        System.out.println("Change password – wire ChangePassword.fxml here");
        buildHistoryList();
    }
}
