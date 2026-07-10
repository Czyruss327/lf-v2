package controller;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class SceneUtil {

    private SceneUtil() {}

    public static void setScene(Stage stage, Parent root) {
        Scene current = stage.getScene();
        boolean wasMaximized = stage.isMaximized();

        if (current == null) {
            stage.setScene(new Scene(root, 960, 700));
        } else {
            stage.setScene(new Scene(root, current.getWidth(), current.getHeight()));
        }

        stage.setMaximized(wasMaximized);
    }
}
