package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import com.campuslf.models.ItemReport;
import com.campuslf.service.ItemService;
import mapper.ItemMapper;
import model.SessionManager;
import model.Item;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * DashboardController
 * Figure 1: admin posts item → appears here as LOST.
 * Figure 2: student browses, admin searches; after claim → item removed.
 */
public class DashboardController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private Button backButton;
    @FXML
    private Button addButton;
    @FXML
    private Button menuButton;
    @FXML
    private ImageView homeIcon;
    @FXML
    private ImageView reportFormIcon;
    @FXML
    private ImageView menuBarIcon;
    @FXML
    private ComboBox<String> filterCombo;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private FlowPane itemGrid;
    @FXML
    private HBox paginationBox;
    @FXML
    private ImageView logoImage;
    @FXML
    private Label dashboardTitleLabel;

    private NavbarHelper navbar;

    private final ItemService itemService = new ItemService();
    private static final int ITEMS_PER_PAGE = 12;
    private int currentPage = 1;
    private String currentFilter = "All";
    private String searchQuery = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo-dashboard.png");
        if (loadImage(reportFormIcon, "/images/report-form.png")) {
            addButton.setText("");
        } else {
            addButton.setGraphic(null);
        }
        if (loadImage(menuBarIcon, "/images/menu-bar.png")) {
            menuButton.setText("");
        } else {
            menuButton.setGraphic(null);
        }
        filterCombo.getItems().addAll("All", "Unclaimed", "Claimed");
        filterCombo.setValue("All");
        navbar = new NavbarHelper(() -> (Stage) searchField.getScene().getWindow());
        if (SessionManager.getInstance().isAdmin()) {
            dashboardTitleLabel.setText("ADMIN DASHBOARD");
            backButton.setVisible(false);
            backButton.setManaged(false);
        } else {
            dashboardTitleLabel.setText("STUDENT DASHBOARD");
            if (loadImage(homeIcon, "/images/home.png")) {
                backButton.setText("");
            } else {
                backButton.setGraphic(null);
            }
            filterCombo.getItems().setAll("Unclaimed");
            filterCombo.setValue("Unclaimed");
            currentFilter = "Unclaimed";
            addButton.setVisible(false);
            addButton.setManaged(false);
            menuButton.setVisible(false);
            menuButton.setManaged(false);
        }

        scrollPane.viewportBoundsProperty().addListener(
                (obs, oldBounds, bounds) -> itemGrid.setPrefWrapLength(Math.max(360, bounds.getWidth() - 16)));
        renderGrid();
    }

    @FXML
    private void onSearch() {
        searchQuery = searchField.getText().trim().toLowerCase();
        currentPage = 1;
        renderGrid();
    }

    @FXML
    private void onFilterChange() {
        currentFilter = filterCombo.getValue() == null ? "All" : filterCombo.getValue();
        currentPage = 1;
        renderGrid();
    }

    /** Figure 1: opens New Post form — admin inputs item details. */
    @FXML
    private void onAddItem() {
        if (!SessionManager.getInstance().isAdmin()) {
            showAlert("Access Denied", "Only admins can post new items.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportForm.fxml"));
            Parent root = loader.load();
            ReportFormController ctrl = loader.getController();
            ctrl.setOnItemSaved(item -> {
                renderGrid();
            });
            Stage stage = (Stage) searchField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle("New Post – PUPSRC Lost and Found");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onMenuToggle() {
        navbar.toggle(menuButton);
    }

    @FXML
    private void onBackToRoleSelection() {
        SessionManager.getInstance().logout();
        navigateTo("/fxml/Login.fxml", "PUPSRC Lost and Found");
    }

    // ── Grid ─────────────────────────────────────────────────

    public void renderGrid() {
        List<Item> filtered = getFilteredItems();
        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));
        if (currentPage > pages)
            currentPage = 1;

        int start = (currentPage - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, total);

        itemGrid.getChildren().clear();
        for (Item item : filtered.subList(start, end)) {
            Node card = buildCard(item);
            if (card != null)
                itemGrid.getChildren().add(card);
        }
        buildPagination(pages);
    }

    private Node buildCard(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ItemCard.fxml"));
            Node card = loader.load();
            ItemCardController ctrl = loader.getController();
            ctrl.setItem(item);
            ctrl.setOnViewDetails(this::openFullDetails);
            return card;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void openFullDetails(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FullDetails.fxml"));
            Parent root = loader.load();
            FullDetailsController ctrl = loader.getController();
            ctrl.setItem(item);
            ctrl.setDashboardController(this);
            Stage stage = (Stage) searchField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle(item.getName() + " – Details");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            SceneUtil.setScene(stage, root);
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Pagination ───────────────────────────────────────────

    private void buildPagination(int pages) {
        paginationBox.getChildren().clear();
        if (pages <= 1)
            return;

        Button prev = new Button("‹ Previous");
        prev.getStyleClass().add("page-btn");
        prev.setDisable(currentPage == 1);
        prev.setOnAction(e -> goPage(currentPage - 1));
        paginationBox.getChildren().add(prev);

        for (int p = 1; p <= pages; p++) {
            if (pages > 5 && p > 2 && p < pages - 1 && Math.abs(p - currentPage) > 1) {
                if (p == 3 || p == pages - 2) {
                    Label dots = new Label("…");
                    dots.getStyleClass().add("page-btn");
                    paginationBox.getChildren().add(dots);
                }
                continue;
            }
            final int pg = p;
            Button btn = new Button(String.valueOf(p));
            btn.getStyleClass().add(p == currentPage ? "page-btn-active" : "page-btn");
            btn.setOnAction(e -> goPage(pg));
            paginationBox.getChildren().add(btn);
        }

        Button next = new Button("Next ›");
        next.getStyleClass().add("page-btn");
        next.setDisable(currentPage == pages);
        next.setOnAction(e -> goPage(currentPage + 1));
        paginationBox.getChildren().add(next);
    }

    private void goPage(int p) {
        int pages = (int) Math.ceil((double) getFilteredItems().size() / ITEMS_PER_PAGE);
        if (p < 1 || p > pages)
            return;
        currentPage = p;
        renderGrid();
    }

    private List<Item> getDatabaseItems() {

        List<Item> items = new java.util.ArrayList<>();

        for (ItemReport report : itemService.getPendingItems()) {
            items.add(ItemMapper.toItem(report));
        }

        if (SessionManager.getInstance().isAdmin()) {

            for (ItemReport report : itemService.getClaimedItems()) {
                items.add(ItemMapper.toItem(report));
            }
        }

        return items;
    }

    private List<Item> getFilteredItems() {

        return getDatabaseItems().stream()

                .filter(i -> SessionManager.getInstance().isAdmin()
                        || i.getStatus() == Item.Status.LOST)

                .filter(i -> {

                    if ("Unclaimed".equals(currentFilter))
                        return i.getStatus() == Item.Status.LOST;

                    if ("Claimed".equals(currentFilter))
                        return i.getStatus() == Item.Status.FOUND;

                    return true;
                })

                .filter(i ->
                        searchQuery.isEmpty()
                                || i.getName().toLowerCase().contains(searchQuery)
                                || i.getLocation().toLowerCase().contains(searchQuery)
                                || i.getColor().toLowerCase().contains(searchQuery))

                .collect(Collectors.toList());
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private boolean loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                iv.setImage(new Image(url.toExternalForm(), true));
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
