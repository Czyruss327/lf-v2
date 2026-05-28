package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory store for Admin objects (singleton).
 * Swap out for a database-backed DAO when persistence is added.
 */
public class AdminStore {

    private static AdminStore instance;
    private final List<Admin> admins = new ArrayList<>();

    private AdminStore() {
        // Seed a default admin for development/testing
        Admin defaultAdmin = new Admin("admin", "admin123");
        defaultAdmin.addHistory(new HistoryEntry("Login Activity", LocalDateTime.now()));
        admins.add(defaultAdmin);
    }

    public static AdminStore getInstance() {
        if (instance == null) {
            instance = new AdminStore();
        }
        return instance;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    public void add(Admin admin) {
        admins.add(admin);
    }

    public boolean exists(String username) {
        return admins.stream()
                .anyMatch(a -> a.getUsername().equalsIgnoreCase(username));
    }

    public Optional<Admin> findByUsername(String username) {
        return admins.stream()
                .filter(a -> a.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    /**
     * Authenticate by username + raw password.
     * Returns the Admin on success, empty on failure.
     */
    public Optional<Admin> authenticate(String username, String password) {
        return findByUsername(username)
                .filter(a -> a.checkPassword(password));
    }

    public List<Admin> getAll() {
        return admins;
    }
}
