package controller;

import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * NavbarController
 *
 * The navbar bar itself is just an HBox (Navbar.fxml).
 * When the host page calls  navbarController.attachDrawerTo(rootStackPane)
 * the controller injects a full-height overlay + drawer into that root,
 * so the popup covers the ENTIRE window — not just the 54 px navbar strip.
 *
 * Host page setup (in its controller's initialize or after fx:include load):
 *
 *   navbarController.attachDrawerTo(rootStackPane);   // <-- required
 *   navbarController.setNavigationHandler(this::onNavigate);
 */
public class NavbarController implements Initializable {

    @FXML private HBox   navbarRoot;   // the HBox declared in Navbar.fxml
    @FXML private Button menuBtn;

    private Consumer<String> navigationHandler;
    private boolean          drawerOpen = false;

    // These are created in Java so they can be placed in the ROOT stack pane
    private Pane  overlay;
    private VBox  drawer;

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buildDrawerNodes();
    }

    /**
     * MUST be called by the host controller after fx:include loads.
     * Attaches the full-height overlay + drawer to the window's root StackPane.
     */
    public void attachDrawerTo(StackPane rootStack) {
        if (!rootStack.getChildren().contains(overlay)) {
            rootStack.getChildren().addAll(overlay, drawer);
        }
        // Keep overlay and drawer hidden initially
        overlay.setVisible(false);
        drawer.setVisible(false);
        drawer.setTranslateX(240); // start off-screen right
    }

    public void setNavigationHandler(Consumer<String> handler) {
        this.navigationHandler = handler;
    }

    // ─── Build drawer nodes programmatically ─────────────────────────────────

    private void buildDrawerNodes() {
        // ── Semi-transparent overlay fills the whole window ──────────────────
        overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.45);");
        // Make it stretch to fill the StackPane
        StackPane.setAlignment(overlay, Pos.TOP_LEFT);
        overlay.prefWidthProperty();   // will be bound after attach
        overlay.setOnMouseClicked(e -> closeDrawer());

        // ── Drawer panel ─────────────────────────────────────────────────────
        drawer = new VBox();
        drawer.setPrefWidth(230);
        drawer.setMaxWidth(230);
        drawer.setStyle(
            "-fx-background-color: white;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.40), 20, 0, -4, 0);"
        );
        StackPane.setAlignment(drawer, Pos.TOP_RIGHT);
        drawer.setPadding(new Insets(60, 0, 20, 0));

        // Nav items
        String[] labels = {"Account", "Dashboard", "Report Form", "Claim Verification"};
        String[] keys   = {"Account", "Dashboard", "ReportForm", "ClaimVerification"};

        for (int i = 0; i < labels.length; i++) {
            Button btn = makeDrawerItem(labels[i], keys[i]);
            drawer.getChildren().add(btn);
        }

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        drawer.getChildren().add(spacer);

        // Log Out
        HBox logoutArea = new HBox();
        logoutArea.setAlignment(Pos.CENTER);
        logoutArea.setPadding(new Insets(14, 0, 10, 0));
        logoutArea.setStyle(
            "-fx-border-color: #e0e0e0 transparent transparent transparent;" +
            "-fx-border-width: 1 0 0 0;"
        );
        Button logoutBtn = new Button("Log Out");
        logoutBtn.setPrefWidth(140);
        logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1a1a1a;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #444444;" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;" +
            "-fx-border-width: 1.5;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 7 20 7 20;"
        );
        logoutBtn.setOnMouseEntered(e -> logoutBtn.setStyle(
            "-fx-background-color: #6B0000;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #6B0000;" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;" +
            "-fx-border-width: 1.5;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 7 20 7 20;"
        ));
        logoutBtn.setOnMouseExited(e -> logoutBtn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1a1a1a;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-border-color: #444444;" +
            "-fx-border-radius: 20;" +
            "-fx-background-radius: 20;" +
            "-fx-border-width: 1.5;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 7 20 7 20;"
        ));
        logoutBtn.setOnAction(e -> handleLogOut());
        logoutArea.getChildren().add(logoutBtn);
        drawer.getChildren().add(logoutArea);
    }

    private Button makeDrawerItem(String label, String navKey) {
        Button btn = new Button(label);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1a1a1a;" +
            "-fx-font-size: 15px;" +
            "-fx-font-family: 'Segoe UI', sans-serif;" +
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-padding: 14 24 14 24;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent transparent #f0f0f0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        );
        btn.setOnMouseEntered(e -> btn.setStyle(
            "-fx-background-color: #f7e8e8;" +
            "-fx-text-fill: #6B0000;" +
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Segoe UI', sans-serif;" +
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-padding: 14 24 14 24;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent transparent #f0f0f0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        ));
        btn.setOnMouseExited(e -> btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #1a1a1a;" +
            "-fx-font-size: 15px;" +
            "-fx-font-family: 'Segoe UI', sans-serif;" +
            "-fx-alignment: CENTER-LEFT;" +
            "-fx-padding: 14 24 14 24;" +
            "-fx-background-radius: 0;" +
            "-fx-cursor: hand;" +
            "-fx-border-color: transparent transparent #f0f0f0 transparent;" +
            "-fx-border-width: 0 0 1 0;"
        ));
        btn.setOnAction(e -> navigate(navKey));
        return btn;
    }

    // ─── Drawer open / close ─────────────────────────────────────────────────

    @FXML
    private void toggleDrawer() {
        if (drawerOpen) closeDrawer();
        else            openDrawer();
    }

    public void closeDrawer() {
        if (!drawerOpen) return;
        drawerOpen = false;

        TranslateTransition slide = new TranslateTransition(Duration.millis(220), drawer);
        slide.setToX(240);
        slide.setOnFinished(e -> {
            drawer.setVisible(false);
            overlay.setVisible(false);
        });
        slide.play();
    }

    private void openDrawer() {
        drawerOpen = true;
        overlay.setVisible(true);
        drawer.setVisible(true);
        drawer.setTranslateX(240);

        TranslateTransition slide = new TranslateTransition(Duration.millis(220), drawer);
        slide.setToX(0);
        slide.play();
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    @FXML private void handleAddItem() { navigate("ReportForm"); }

    private void navigate(String page) {
        closeDrawer();
        if (navigationHandler != null) navigationHandler.accept(page);
    }

    private void handleLogOut() {
        closeDrawer();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Stage s = (Stage) navbarRoot.getScene().getWindow();
            s.setScene(new Scene(root));
            s.setTitle("PUPSRC Lost and Found");
            s.setMaximized(false);
            s.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
