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
import model.Item;
import model.ItemStore;
import model.SessionManager;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * DashboardController
 * Figure 1: admin posts item → appears here as LOST.
 * Figure 2: student browses, admin searches; after claim → item removed.
 */
public class DashboardController implements Initializable {

    @FXML private TextField        searchField;
    @FXML private Button           menuButton;
    @FXML private ComboBox<String> filterCombo;
    @FXML private FlowPane         itemGrid;
    @FXML private HBox             paginationBox;
    @FXML private ImageView        logoImage;

    private ContextMenu hamburgerMenu;

    private static final int ITEMS_PER_PAGE = 12;
    private int    currentPage   = 1;
    private String currentFilter = "All";
    private String searchQuery   = "";

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        filterCombo.getItems().addAll("All", "Lost", "Found");
        filterCombo.setValue("All");
        buildHamburgerMenu();
        renderGrid();
    }

    @FXML private void onSearch() {
        searchQuery = searchField.getText().trim().toLowerCase();
        currentPage = 1;
        renderGrid();
    }

    @FXML private void onFilterChange() {
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
                // Figure 1: system saves → item posted to dashboard as LOST
                ItemStore.getInstance().addItem(item);
                renderGrid();
            });
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("New Post – PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    /** Builds the hamburger ContextMenu once and reuses it. */
    private void buildHamburgerMenu() {
        hamburgerMenu = new ContextMenu();
        hamburgerMenu.getStyleClass().add("hamburger-menu");

        // Account
        MenuItem accountItem = new MenuItem("👤  Account");
        accountItem.getStyleClass().add("menu-item-styled");
        accountItem.setOnAction(e -> navigateTo("/fxml/Account.fxml", "Account – PUPSRC Lost and Found"));

        // Dashboard
        MenuItem dashItem = new MenuItem("🏠  Dashboard");
        dashItem.getStyleClass().add("menu-item-styled");
        dashItem.setOnAction(e -> navigateTo("/fxml/Dashboard.fxml", "PUPSRC Lost and Found"));

        // Report Form
        MenuItem reportItem = new MenuItem("📋  Report Form");
        reportItem.getStyleClass().add("menu-item-styled");
        reportItem.setOnAction(e -> navigateTo("/fxml/ReportForm.fxml", "Report Form – PUPSRC Lost and Found"));

        // Claim Verification
        MenuItem claimItem = new MenuItem("✅  Claim Verification");
        claimItem.getStyleClass().add("menu-item-styled");
        claimItem.setOnAction(e -> navigateTo("/fxml/ClaimVerification.fxml", "Claim Verification – PUPSRC Lost and Found"));

        // Separator
        SeparatorMenuItem sep = new SeparatorMenuItem();

        // Log Out
        MenuItem logoutItem = new MenuItem("↩  Log Out");
        logoutItem.getStyleClass().addAll("menu-item-styled", "menu-item-logout");
        logoutItem.setOnAction(e -> handleLogout());

        hamburgerMenu.getItems().addAll(accountItem, dashItem, reportItem, claimItem, sep, logoutItem);
    }

    @FXML
    private void onMenuToggle() {
        if (hamburgerMenu == null) buildHamburgerMenu();
        if (hamburgerMenu.isShowing()) {
            hamburgerMenu.hide();
        } else {
            hamburgerMenu.show(menuButton, javafx.geometry.Side.BOTTOM, 0, 4);
        }
    }

    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Log Out");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to log out?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            navigateTo("/fxml/Login.fxml", "PUPSRC Lost and Found");
        }
    }

    private void navigateTo(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ── Grid ─────────────────────────────────────────────────

    public void renderGrid() {
        List<Item> filtered = getFilteredItems();
        int total = filtered.size();
        int pages = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));
        if (currentPage > pages) currentPage = 1;

        int start = (currentPage - 1) * ITEMS_PER_PAGE;
        int end   = Math.min(start + ITEMS_PER_PAGE, total);

        itemGrid.getChildren().clear();
        for (Item item : filtered.subList(start, end)) {
            Node card = buildCard(item);
            if (card != null) itemGrid.getChildren().add(card);
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
        } catch (IOException e) { e.printStackTrace(); return null; }
    }

    private void openFullDetails(Item item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FullDetails.fxml"));
            Parent root = loader.load();
            FullDetailsController ctrl = loader.getController();
            ctrl.setItem(item);
            ctrl.setDashboardController(this);
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(item.getName() + " – Details");
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Pagination ───────────────────────────────────────────

    private void buildPagination(int pages) {
        paginationBox.getChildren().clear();
        if (pages <= 1) return;

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
        if (p < 1 || p > pages) return;
        currentPage = p;
        renderGrid();
    }

    private List<Item> getFilteredItems() {
        return ItemStore.getInstance().getItems().stream()
                .filter(i -> {
                    if ("Lost".equals(currentFilter))  return i.getStatus() == Item.Status.LOST;
                    if ("Found".equals(currentFilter)) return i.getStatus() == Item.Status.FOUND;
                    return true;
                })
                .filter(i -> searchQuery.isEmpty()
                        || i.getName().toLowerCase().contains(searchQuery)
                        || i.getLocation().toLowerCase().contains(searchQuery)
                        || i.getColor().toLowerCase().contains(searchQuery))
                .collect(Collectors.toList());
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {}
    }
}
