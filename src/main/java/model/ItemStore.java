package model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ItemStore — singleton shared item list.
 * Figure 1: admin posts item → added here with status LOST.
 * Figure 2: claim confirmed → item removed from here (removed from public
 * dashboard).
 */
public class ItemStore {

    private static ItemStore instance;
    private final ObservableList<Item> items = FXCollections.observableArrayList();
    private static int nextId = 100;

    private ItemStore() {
        loadSampleData();
    }

    public static ItemStore getInstance() {
        if (instance == null)
            instance = new ItemStore();
        return instance;
    }

    public ObservableList<Item> getItems() {
        return items;
    }

    /** Figure 1: admin inputs item → system saves → posted as LOST. */
    public void addItem(Item item) {
        item.setId(nextId++);
        items.add(item);
        AuditLog.logNewPost(item.getName(),
                SessionManager.getInstance().getUsername() != null
                        ? SessionManager.getInstance().getUsername()
                        : "admin");
    }

    /** Figure 2: claim confirmed → system removes post from public dashboard. */
    public void removeItem(Item item) {
        items.remove(item);
    }

    /** Figure 2: update status to CLAIMED in the store. */
    public void markAsClaimed(Item item, String claimantName) {
        item.setStatus(Item.Status.FOUND);
        AuditLog.logClaim(item.getName(), claimantName,
                SessionManager.getInstance().getUsername() != null
                        ? SessionManager.getInstance().getUsername()
                        : "admin");
        // Figure 2: remove item's post from public dashboard
        removeItem(item);
    }

    private void loadSampleData() {
        Item w = new Item(1, "Wallet", Item.Status.LOST, "Brown", "March 24, 2026", "Court Bleacher",
                "/images/wallet.jpg");
        w.setReporterName("Dela Cruz, Juan");
        w.setStudentId("2023-00000-SR-0");
        w.setContactNumber("0912-345-6789");
        w.setCategory("Bags & Wallets");
        items.add(w);
        Item p = new Item(2, "Android Phone", Item.Status.LOST, "Silver", "March 15, 2026", "Main Lobby",
                "/images/phone.jpg");
        p.setCategory("Electronics");
        items.add(p);
        Item id1 = new Item(3, "School ID", Item.Status.LOST, "Red", "March 25, 2026", "Room 201", "/images/id.jpg");
        id1.setCategory("IDs & Documents");
        items.add(id1);
        Item aq = new Item(4, "Aquaflask", Item.Status.LOST, "Blue", "April 21, 2026", "Kubo", "/images/aquaflask.jpg");
        aq.setCategory("Accessories");
        items.add(aq);
        items.add(new Item(5, "Notebooks", Item.Status.LOST, "White/Maroon", "April 01, 2026", "Room 302",
                "/images/notebook.jpg"));
        items.add(new Item(6, "Wallet", Item.Status.LOST, "Pink Coach", "May 12, 2026", "Court Bleacher",
                "/images/wallet2.jpg"));
        items.add(
                new Item(7, "Laptop", Item.Status.LOST, "Silver Acer", "March 19, 2026", "Kubo", "/images/laptop.jpg"));
        items.add(new Item(8, "Book", Item.Status.LOST, "Yellow", "April 6, 2026", "Court", "/images/book.jpg"));
        items.add(
                new Item(9, "iPhone", Item.Status.LOST, "Silver", "April 14, 2026", "Room 303", "/images/iphone.jpg"));
        items.add(new Item(10, "Umbrella", Item.Status.LOST, "Green", "May 20, 2026", "Court", "/images/umbrella.jpg"));
        items.add(new Item(11, "School ID", Item.Status.LOST, "Red", "May 05, 2026", "Registrar Window",
                "/images/id2.jpg"));
        items.add(new Item(12, "BAG", Item.Status.LOST, "Black", "May 20, 2026", "Court", "/images/bag.jpg"));
        items.add(new Item(13, "Aquaflask", Item.Status.FOUND, "Gray", "May 19, 2026", "Room 212",
                "/images/aquaflask2.jpg"));
        items.add(new Item(14, "Bag", Item.Status.FOUND, "Black", "March 20, 2026", "Court", "/images/bag2.jpg"));
        items.add(new Item(15, "Pouch", Item.Status.FOUND, "Black", "May 08, 2026", "Kubo", "/images/pouch.jpg"));
        items.add(new Item(16, "Document", Item.Status.FOUND, "White", "April 18, 2026", "Court",
                "/images/document.jpg"));
        items.add(new Item(17, "Passport", Item.Status.FOUND, "Maroon", "May 06, 2026", "Court Bleacher",
                "/images/passport.jpg"));
        items.add(new Item(18, "Watch", Item.Status.FOUND, "Gold", "May 07, 2026", "Court", "/images/watch.jpg"));
        items.add(new Item(19, "Powerbank", Item.Status.FOUND, "Silver", "May 12, 2026", "Court",
                "/images/powerbank.jpg"));
        items.add(new Item(20, "Binder", Item.Status.FOUND, "Blue/Black", "March 25, 2026", "Court Bleacher",
                "/images/binder.jpg"));
    }
}
