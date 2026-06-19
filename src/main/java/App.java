import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Main entry point — opens the Login screen first.
 */

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        StartupDiagnostics.install();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("PUPSRC Lost and Found");
            primaryStage.setScene(new Scene(root, 960, 700));
            primaryStage.setMinWidth(860);
            primaryStage.setMinHeight(620);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            primaryStage.show();
            StartupDiagnostics.log("Login window shown.");
        } catch (Throwable error) {
            StartupDiagnostics.log("Unable to show login window", error);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Campus Lost and Found");
            alert.setHeaderText("The app could not start.");
            alert.setContentText(error + "\n\nLog file:\n" + StartupDiagnostics.getLogFile());
            alert.showAndWait();
            throw error;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
