package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import model.Item;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ItemDetailController
 * Shows full details of a selected item.
 */
public class ItemDetailController implements Initializable {

    @FXML
    private Label detailTitle;
    @FXML
    private ImageView detailImage;
    @FXML
    private Label detailStatusBadge;
    @FXML
    private Label detailName;
    @FXML
    private Label detailColor;
    @FXML
    private Label detailDate;
    @FXML
    private Label detailLocation;
    @FXML
    private Label detailStatus;

    private Item item;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    public void setItem(Item item) {
        this.item = item;

        detailTitle.setText(item.getName() + " – Details");
        detailName.setText(item.getName());
        detailColor.setText(item.getColor());
        detailDate.setText(item.getDate());
        detailLocation.setText(item.getLocation());
        detailStatus.setText(item.getStatusLabel());

        // Badge
        detailStatusBadge.setText(item.getStatusLabel());
        detailStatusBadge.getStyleClass().removeAll("badge-lost", "badge-found");
        detailStatusBadge.getStyleClass().addAll(
                "badge",
                item.getStatus() == Item.Status.LOST ? "badge-lost" : "badge-found");

        // Image
        loadImage(item.getImagePath());
    }

    @FXML
    private void onClaimItem() {
        // TODO: implement claim workflow (e.g., open a claim form or mark as claimed)
        System.out.println("Claim item: " + (item != null ? item.getName() : "null"));
    }

    @FXML
    private void onClose() {
        ((Stage) detailTitle.getScene().getWindow()).close();
    }

    private void loadImage(String path) {
        if (path == null || path.isBlank())
            return;
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                detailImage.setImage(new Image(url.toExternalForm(), true));
            }
        } catch (Exception e) {
            // Image not found — placeholder background remains
        }
    }
}
