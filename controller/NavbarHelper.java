package controller;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * NavbarHelper — shared hamburger-menu logic for every page.
 * Usage:
 *   private NavbarHelper navbar;
 *
 *   // In initialize():
 *   navbar = new NavbarHelper(() -> (Stage) anyNode.getScene().getWindow());
 *
 *   // In @FXML onMenu():
 *   navbar.toggle(menuButton);
 */
public class NavbarHelper {

    public interface StageSupplier { Stage get(); }

    private final StageSupplier stageSupplier;
    private ContextMenu menu;

    public NavbarHelper(StageSupplier stageSupplier) {
        this.stageSupplier = stageSupplier;
        build();
    }

    /** Show or hide the dropdown below the given button. */
    public void toggle(Button anchor) {
        if (menu.isShowing()) { menu.hide(); return; }
        menu.show(anchor, Side.BOTTOM, 0, 4);
    }

    // ── Private ──────────────────────────────────────────────

    private void build() {
        menu = new ContextMenu();
        menu.getStyleClass().add("hamburger-menu");

        menu.getItems().addAll(
            item("👤  Account",             () -> go("/fxml/Account.fxml",            "Account – PUPSRC Lost and Found")),
            item("🏠  Dashboard",            () -> go("/fxml/Dashboard.fxml",          "PUPSRC Lost and Found")),
            item("📋  Report Form",          () -> go("/fxml/ReportForm.fxml",         "Report Form – PUPSRC Lost and Found")),
            item("✅  Claim Verification",   () -> go("/fxml/ClaimVerification.fxml",  "Claim Verification – PUPSRC Lost and Found")),
            new SeparatorMenuItem(),
            logoutItem()
        );
    }

    private MenuItem item(String label, Runnable action) {
        MenuItem mi = new MenuItem(label);
        mi.getStyleClass().add("menu-item-styled");
        mi.setOnAction(e -> action.run());
        return mi;
    }

    private MenuItem logoutItem() {
        MenuItem mi = new MenuItem("↩  Log Out");
        mi.getStyleClass().addAll("menu-item-styled", "menu-item-logout");
        mi.setOnAction(e -> handleLogout());
        return mi;
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Log Out");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            model.SessionManager.getInstance().logout();
            go("/fxml/Login.fxml", "PUPSRC Lost and Found");
        }
    }

    private void go(String fxml, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = stageSupplier.get();
            SceneUtil.setScene(stage, root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
