package controller;

import com.campuslf.service.AuthenticationService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * CreateAdminAccountController
 * Figure 3 — NO branch.
 * Admin fills required fields → system saves → access granted.
 */
public class CreateAdminAccountController implements Initializable {

    private final AuthenticationService authenticationService = new AuthenticationService();

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;
    @FXML
    private ImageView bgImage;
    @FXML
    private ImageView logoImage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setText("");
        loadImage(bgImage, "/images/campus_bg.jpg");
        loadImage(logoImage, "/images/logo.png");
    }

    @FXML
    private void onCreateAccount() {
        errorLabel.setText("");

        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username.isEmpty()) {
            errorLabel.setText("Username is required.");
            return;
        }
        if (password.isEmpty()) {
            errorLabel.setText("Password is required.");
            return;
        }
        if (!password.equals(confirm)) {
            errorLabel.setText("Passwords do not match.");
            return;
        }
        if (password.length() < 6) {
            errorLabel.setText("Password must be at least 6 characters.");
            return;
        }

        // Check username not already taken
        if (authenticationService.usernameExists(username)) {
            errorLabel.setText("Username already taken. Choose another.");
            return;
        }

        if (!confirmLogoutWarning()) {
            return;
        }

        // Figure 3: system saves the information
        if (!authenticationService.createAdmin(username, password)) {
            errorLabel.setText("Unable to create account. Please try again.");
            return;
        }

        // Log in with new account immediately → access granted
        SessionManager.getInstance().logout();
        showAccountCreatedMessage();
        navigateTo("/fxml/AdminLogin.fxml", "Admin Login - PUPSRC Lost and Found");
    }

    @FXML
    private void onBackToLogin() {
        navigateTo("/fxml/AdminLogin.fxml", "Admin Login – PUPSRC Lost and Found");
    }

    @FXML
    private void onHome() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/Login.fxml", "PUPSRC Lost and Found");
    }

    private boolean confirmLogoutWarning() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Create Admin Account");
        alert.setHeaderText("You will be logged out after creating this account.");
        alert.setContentText("Continue creating the admin account?");
        return alert.showAndWait()
                .filter(ButtonType.OK::equals)
                .isPresent();
    }

    private void showAccountCreatedMessage() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Account Created");
        alert.setHeaderText(null);
        alert.setContentText("Admin account created. Please log in again.");
        alert.showAndWait();
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle("PUPSRC Lost and Found – Admin");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String path, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle(title);
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
