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

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class DashboardController implements Initializable {

    @FXML private TextField   searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private FlowPane    itemGrid;
    @FXML private HBox        paginationBox;
    @FXML private ImageView   logoImage;

    private static final int ITEMS_PER_PAGE = 12;
    private int currentPage = 1;
    private String currentFilter = "All";
    private String searchQuery   = "";

    private final List<Item> allItems = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadImage(logoImage, "/images/logo.png");
        filterCombo.getItems().addAll("All", "Lost", "Found");
        filterCombo.setValue("All");
        loadSampleData();
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

    @FXML
    private void onAddItem() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ReportForm.fxml"));
            Parent root = loader.load();
            ReportFormController ctrl = loader.getController();
            ctrl.setOnItemSaved(item -> { allItems.add(item); renderGrid(); });
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Report Form – PUPSRC Lost and Found");
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void onMenuToggle() { System.out.println("Menu toggled"); }

    // ── Grid ─────────────────────────────────────────────────

    private void renderGrid() {
        List<Item> filtered = getFilteredItems();
        int total  = filtered.size();
        int pages  = Math.max(1, (int) Math.ceil((double) total / ITEMS_PER_PAGE));
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
            Stage stage = (Stage) searchField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(item.getName() + " – PUPSRC Lost and Found");
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
        return allItems.stream()
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

    private void loadImage(ImageView iv, String path) {
        try {
            URL url = getClass().getResource(path);
            if (url != null) iv.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ignored) {}
    }

    private void loadSampleData() {
        Item w = new Item(1,"Wallet",Item.Status.LOST,"Brown","March 24, 2026","Court Bleacher","/images/wallet.jpg");
        w.setReporterName("Dela Cruz, Juan"); w.setStudentId("2023-00000-SR-0"); w.setContactNumber("0912-345-6789"); w.setCategory("Bags & Wallets");
        allItems.add(w);
        allItems.add(new Item(2,"Android Phone",Item.Status.LOST,"Silver","March 15, 2026","School","/images/phone.jpg"));
        allItems.add(new Item(3,"School ID",Item.Status.LOST,"Red","March 25, 2026","Room 201","/images/id.jpg"));
        allItems.add(new Item(4,"Aquaflask",Item.Status.LOST,"Blue","April 21, 2026","Kubo","/images/aquaflask.jpg"));
        allItems.add(new Item(5,"Notebooks",Item.Status.LOST,"White/Maroon","April 01, 2026","Room 302","/images/notebook.jpg"));
        allItems.add(new Item(6,"Wallet",Item.Status.LOST,"Pink Coach","May 12, 2026","Court Bleacher","/images/wallet2.jpg"));
        allItems.add(new Item(7,"Laptop",Item.Status.LOST,"Silver Acer","March 19, 2026","Kubo","/images/laptop.jpg"));
        allItems.add(new Item(8,"Book",Item.Status.LOST,"Yellow","April 6, 2026","Court","/images/book.jpg"));
        allItems.add(new Item(9,"iPhone",Item.Status.LOST,"Silver","April 14, 2026","Room 303","/images/iphone.jpg"));
        allItems.add(new Item(10,"Umbrella",Item.Status.LOST,"Green","May 20, 2026","Court","/images/umbrella.jpg"));
        allItems.add(new Item(11,"School ID",Item.Status.LOST,"Red","May 05, 2026","School","/images/id2.jpg"));
        allItems.add(new Item(12,"BAG",Item.Status.LOST,"Black","May 20, 2026","Court","/images/bag.jpg"));
        allItems.add(new Item(13,"Aquaflask",Item.Status.FOUND,"Gray","May 19, 2026","Room 212","/images/aquaflask2.jpg"));
        allItems.add(new Item(14,"Bag",Item.Status.FOUND,"Black","March 20, 2026","Court","/images/bag2.jpg"));
        allItems.add(new Item(15,"Pouch",Item.Status.FOUND,"Black","May 08, 2026","Kubo","/images/pouch.jpg"));
        allItems.add(new Item(16,"Document",Item.Status.FOUND,"White","April 18, 2026","Court","/images/document.jpg"));
        allItems.add(new Item(17,"Passport",Item.Status.FOUND,"Maroon","May 06, 2026","Court Bleacher","/images/passport.jpg"));
        allItems.add(new Item(18,"Watch",Item.Status.FOUND,"Gold","May 07, 2026","Court","/images/watch.jpg"));
        allItems.add(new Item(19,"Powerbank",Item.Status.FOUND,"Silver","May 12, 2026","Court","/images/powerbank.jpg"));
        allItems.add(new Item(20,"Binder",Item.Status.FOUND,"Blue/Black","March 25, 2026","Court Bleacher","/images/binder.jpg"));
    }
}
