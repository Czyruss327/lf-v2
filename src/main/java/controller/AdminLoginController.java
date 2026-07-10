package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.campuslf.models.Admin;
import com.campuslf.service.ActivityLogService;
import com.campuslf.service.AuthenticationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.SessionManager;

/**
 * AdminLoginController - admin authentication flow.
 * Admin logs in with registered account saved in the database.
 */
public class AdminLoginController implements Initializable {

    private final AuthenticationService authenticationService = new AuthenticationService();
    private final ActivityLogService activityLogService = new ActivityLogService();

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
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
        passwordField.setOnAction(e -> onLogin());
    }

    /** Authenticate credentials and grant admin dashboard access. */

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        Admin admin = authenticationService.authenticate(user, pass);

        if (admin == null) {
            errorLabel.setText("Invalid username or password.");
            passwordField.clear();
            return;
        }

        // Set session so admin-only screens and actions can use the account id.
        String sessionUsername = admin.getUsername() == null ? user : admin.getUsername();
        SessionManager.getInstance().login(
                SessionManager.Role.ADMIN,
                sessionUsername,
                admin.getAdminId());
        activityLogService.logAction(
                Math.max(1, SessionManager.getInstance().getAdminId()),
                "Admin logged in: " + sessionUsername);
        navigateToDashboard();
    }

    /** Navigate to create account. */
    @FXML
    private void onCreateAccount() {
        navigateTo("/fxml/CreateAdminAccount.fxml", "Create Admin Account - PUPSRC Lost and Found");
    }

    @FXML
    private void onBack() {
        navigateTo("/fxml/Login.fxml", "PUPSRC Lost and Found");
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle("PUPSRC Lost and Found - Admin");
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
