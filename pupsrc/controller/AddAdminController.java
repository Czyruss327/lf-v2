package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import model.Admin;
import model.AdminStore;
import model.HistoryEntry;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AddAdminController implements Initializable {

    @FXML private StackPane        rootStack;
    @FXML private NavbarController navbarController;

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         statusLabel;

    private Admin sourceAdmin;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statusLabel.setText("");

        javafx.application.Platform.runLater(() -> {
            navbarController.attachDrawerTo(rootStack);
            navbarController.setNavigationHandler(page -> closeWindow());
        });

        usernameField.textProperty().addListener((o, a, b) -> statusLabel.setText(""));
        passwordField.textProperty().addListener((o, a, b) -> statusLabel.setText(""));
    }

    public void setSourceAdmin(Admin admin) {
        this.sourceAdmin = admin;
    }

    @FXML
    private void handleConfirm() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty()) { showError("Username cannot be empty."); return; }
        if (password.length() < 6) { showError("Password must be at least 6 characters."); return; }
        if (AdminStore.getInstance().exists(username)) {
            showError("Username \"" + username + "\" is already taken."); return;
        }

        Admin newAdmin = new Admin(username, password);
        newAdmin.addHistory(new HistoryEntry("Account Created", LocalDateTime.now()));
        AdminStore.getInstance().add(newAdmin);

        if (sourceAdmin != null)
            sourceAdmin.addHistory(new HistoryEntry("Added Admin: " + username, LocalDateTime.now()));

        closeWindow();
    }

    @FXML private void handleCancel() { closeWindow(); }

    private void showError(String msg) { statusLabel.setText(msg); }

    private void closeWindow() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }
}
