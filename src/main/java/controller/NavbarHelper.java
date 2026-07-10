package controller;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.SessionManager;

import java.io.IOException;
import java.util.Optional;

public class NavbarHelper {

    public interface StageSupplier {
        Stage get();
    }

    private final StageSupplier stageSupplier;
    private ContextMenu menu;

    public NavbarHelper(StageSupplier stageSupplier) {
        this.stageSupplier = stageSupplier;
        build();
    }

    public void toggle(Button anchor) {
        if (menu.isShowing()) {
            menu.hide();
            return;
        }
        menu.show(anchor, Side.BOTTOM, 0, 4);
    }

    private void build() {
        menu = new ContextMenu();
        menu.getStyleClass().add("hamburger-menu");

        menu.getItems().add(item("Dashboard", () -> go("/fxml/Dashboard.fxml", "PUPSRC Lost and Found")));
        menu.getItems().add(item("Account", () -> go("/fxml/Account.fxml", "Account - PUPSRC Lost and Found")));

        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(logoutItem());
    }

    private MenuItem item(String label, Runnable action) {
        MenuItem mi = new MenuItem(label);
        mi.getStyleClass().add("menu-item-styled");
        mi.setOnAction(e -> action.run());
        return mi;
    }

    private MenuItem logoutItem() {
        MenuItem mi = new MenuItem("Log Out");
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
            SessionManager.getInstance().logout();
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
