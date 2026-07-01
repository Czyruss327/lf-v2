package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import model.Item;
import model.SessionManager;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * ItemCardController
 * ADMIN → VIEW DETAILS button visible
 * STUDENT → VIEW DETAILS button hidden (read-only browse)
 */
public class ItemCardController implements Initializable {

    @FXML
    private ImageView itemImage;
    @FXML
    private Label statusBadge;
    @FXML
    private Label itemName;
    @FXML
    private Label itemColor;
    @FXML
    private Label itemCategory;
    @FXML
    private Label itemDate;
    @FXML
    private Label itemLocation;
    @FXML
    private Button viewDetailsBtn;

    private Item item;
    private Consumer<Item> onViewDetails;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Only admins can view full details
        boolean isAdmin = SessionManager.getInstance().isAdmin();
        viewDetailsBtn.setVisible(isAdmin);
        viewDetailsBtn.setManaged(isAdmin);
    }

    public void setItem(Item item) {
        this.item = item;

        itemName.setText(item.getName());
        itemColor.setText("Color: " + valueOrDash(item.getColor()));
        itemCategory.setText("Category: " + valueOrDash(item.getCategory()));
        itemDate.setText("Posted: " + valueOrDash(item.getDate()));
        itemLocation.setText(locationPrefix(item) + ": " + valueOrDash(item.getLocation()));

        statusBadge.setText(item.getStatusLabel());
        statusBadge.getStyleClass().removeAll("badge-lost", "badge-found", "badge-claimed", "badge-resolved");
        statusBadge.getStyleClass().addAll(
                "badge",
                badgeStyle(item));

        loadImage(item.getImagePath());
    }

    private String locationPrefix(Item item) {
        return item.getStatus() == Item.Status.LOST || item.getStatus() == Item.Status.RESOLVED
                ? "Lost"
                : "Found";
    }

    private String badgeStyle(Item item) {
        return switch (item.getStatus()) {
            case LOST -> "badge-lost";
            case CLAIMED -> "badge-claimed";
            case RESOLVED -> "badge-resolved";
            default -> "badge-found";
        };
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    public void setOnViewDetails(Consumer<Item> callback) {
        this.onViewDetails = callback;
    }

    @FXML
    private void onViewDetails() {
        if (onViewDetails != null && item != null) {
            onViewDetails.accept(item);
        }
    }

    private void loadImage(String path) {
        if (path == null || path.isBlank())
            return;
        try {
            URL url = getClass().getResource(path);
            String uri = path.startsWith("file:") ? path : (url != null ? url.toExternalForm() : null);
            if (uri != null)
                itemImage.setImage(new Image(uri, true));
        } catch (Exception ignored) {
        }
    }
}
