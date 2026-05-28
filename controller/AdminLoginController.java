package controller;

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
import model.UserAccount;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * AdminLoginController
 * Authenticates admin users using UserAccount credentials.
 *
 * DEFAULT ADMIN ACCOUNTS:
 *   Username: admin        Password: admin123
 *   Username: pupsrc_admin Password: pup2026
 */
public class AdminLoginController implements Initializable {

    @FXML private TextField     usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label         errorLabel;
    @FXML private ImageView     bgImage;
    @FXML private ImageView     logoImage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setText("");
        loadImage(bgImage,   "/images/campus_bg.jpg");
        loadImage(logoImage, "/images/logo.png");
        passwordField.setOnAction(e -> onLogin());
    }

    @FXML
    private void onLogin() {
        errorLabel.setText("");
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        UserAccount account = UserAccount.authenticate(user, pass);

        if (account == null) {
            errorLabel.setText("Invalid username or password.");
            passwordField.clear();
            return;
        }

        if (account.getRole() != SessionManager.Role.ADMIN) {
            errorLabel.setText("This login is for admins only.");
            passwordField.clear();
            return;
        }

        // Set session
        SessionManager.getInstance().login(SessionManager.Role.ADMIN, user);
        navigateToDashboard();
    }

    @FXML
    private void onForgotPassword() {
        errorLabel.setText("Please contact your system administrator.");
    }

    private void navigateToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root, 960, 700));
            stage.setTitle("PUPSRC Lost and Found – Admin");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {}
    }
}
