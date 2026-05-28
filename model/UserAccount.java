package model;

import java.util.ArrayList;
import java.util.List;

/**
 * UserAccount — stores user credentials.
 * In a real app, replace this with a database lookup.
 *
 * DEFAULT ACCOUNTS:
 * ┌─────────────┬──────────────┬──────────┐
 * │ Role        │ Username     │ Password │
 * ├─────────────┼──────────────┼──────────┤
 * │ ADMIN       │ admin        │ admin123 │
 * │ ADMIN       │ pupsrc_admin │ pup2026  │
 * │ STUDENT     │ student      │ student1 │
 * └─────────────┴──────────────┴──────────┘
 */
public class UserAccount {

    private final String username;
    private final String password;
    private final SessionManager.Role role;

    public UserAccount(String username, String password, SessionManager.Role role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public SessionManager.Role getRole() { return role; }

    // ── Default accounts ─────────────────────────────────────

    private static final List<UserAccount> ACCOUNTS = new ArrayList<>();

    static {
        ACCOUNTS.add(new UserAccount("admin",        "admin123", SessionManager.Role.ADMIN));
        ACCOUNTS.add(new UserAccount("pupsrc_admin", "pup2026",  SessionManager.Role.ADMIN));
        ACCOUNTS.add(new UserAccount("student",      "student1", SessionManager.Role.STUDENT));
    }

    /**
     * Authenticate a user. Returns the matching account or null if not found.
     */
    public static UserAccount authenticate(String username, String password) {
        for (UserAccount acc : ACCOUNTS) {
            if (acc.username.equals(username) && acc.password.equals(password)) {
                return acc;
            }
        }
        return null;
    }
}
