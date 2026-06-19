package model;

import java.util.ArrayList;
import java.util.List;

/**
 * UserAccount — user credentials store.
 *
 * Figure 3: supports both login (YES branch) and create account (NO branch).
 *
 * DEFAULT ADMIN ACCOUNTS:
 * ┌──────────────┬──────────┬───────┐
 * │ Username │ Password │ Role │
 * ├──────────────┼──────────┼───────┤
 * │ admin │ admin123 │ ADMIN │
 * │ pupsrc_admin │ pup2026 │ ADMIN │
 * └──────────────┴──────────┴───────┘
 */
public class UserAccount {

    private final String username;
    private final String password;
    private final SessionManager.Role role;

    private UserAccount(String username, String password, SessionManager.Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public SessionManager.Role getRole() {
        return role;
    }

    // ── In-memory account store ───────────────────────────────
    private static final List<UserAccount> ACCOUNTS = new ArrayList<>();

    static {
        ACCOUNTS.add(new UserAccount("admin", "admin123", SessionManager.Role.ADMIN));
        ACCOUNTS.add(new UserAccount("pupsrc_admin", "pup2026", SessionManager.Role.ADMIN));
        ACCOUNTS.add(new UserAccount("123", "123", SessionManager.Role.ADMIN));
    }

    /**
     * Figure 3 — YES branch: authenticate existing admin.
     * Returns the matching account or null.
     */
    public static UserAccount authenticate(String username, String password) {
        for (UserAccount acc : ACCOUNTS) {
            if (acc.username.equals(username) && acc.password.equals(password)) {
                return acc;
            }
        }
        return null;
    }

    /**
     * Figure 3 — check if username already exists.
     */
    public static boolean usernameExists(String username) {
        for (UserAccount acc : ACCOUNTS) {
            if (acc.username.equalsIgnoreCase(username))
                return true;
        }
        return false;
    }

    /**
     * Figure 3 — NO branch: system saves new admin account.
     */
    public static void addAccount(String username, String password, SessionManager.Role role) {
        ACCOUNTS.add(new UserAccount(username, password, role));
    }
}
