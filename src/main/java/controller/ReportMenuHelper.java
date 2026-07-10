package controller;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

import java.io.IOException;

public class ReportMenuHelper {

    public interface StageSupplier {
        Stage get();
    }

    private final StageSupplier stageSupplier;
    private final ContextMenu menu;

    public ReportMenuHelper(StageSupplier stageSupplier) {
        this.stageSupplier = stageSupplier;
        this.menu = buildMenu();
    }

    public void toggle(Button anchor) {
        if (menu.isShowing()) {
            menu.hide();
            return;
        }
        menu.show(anchor, Side.BOTTOM, 0, 4);
    }

    private ContextMenu buildMenu() {
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getStyleClass().add("hamburger-menu");
        contextMenu.getItems().add(item("Report Found Item", () -> openReport(true)));
        contextMenu.getItems().add(item("Report Lost Item", () -> openReport(false)));
        return contextMenu;
    }

    private MenuItem item(String label, Runnable action) {
        MenuItem menuItem = new MenuItem(label);
        menuItem.getStyleClass().add("menu-item-styled");
        menuItem.setOnAction(e -> action.run());
        return menuItem;
    }

    private void openReport(boolean foundMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportForm.fxml"));
            Parent root = loader.load();
            ReportFormController controller = loader.getController();
            if (foundMode) {
                controller.setFoundReportMode();
            } else {
                controller.setLostReportMode();
            }

            Stage stage = stageSupplier.get();
            SceneUtil.setScene(stage, root);
            stage.setTitle((foundMode ? "Report Found Item" : "Report Lost Item") + " - PUPSRC Lost and Found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
