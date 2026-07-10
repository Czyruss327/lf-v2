package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private ImageView bgImage;
    @FXML
    private ImageView logoImage;
    @FXML
    private Button adminBtn;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(bgImage, "/images/campus_bg.jpg");
        loadImage(logoImage, "/images/logo.png");
    }

    @FXML
    private void onAdminLogin() {
        navigateTo("/fxml/AdminLogin.fxml", "Admin Login – PUPSRC Lost and Found");
    }


    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) adminBtn.getScene().getWindow();
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
