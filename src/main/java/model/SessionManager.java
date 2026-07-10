package model;

public class SessionManager {

    public enum Role {
        ADMIN, STUDENT
    }

    private static SessionManager instance;
    private Role currentRole;
    private String currentUsername;
    private int currentAdminId;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }

    public void login(Role role, String username) {
        login(role, username, 0);
    }

    public void login(Role role, String username, int adminId) {
        this.currentRole = role;
        this.currentUsername = username;
        this.currentAdminId = adminId;
    }

    public void logout() {
        this.currentRole = null;
        this.currentUsername = null;
        this.currentAdminId = 0;
    }

    public Role getRole() {
        return currentRole;
    }

    public String getUsername() {
        return currentUsername;
    }

    public int getAdminId() {
        return currentAdminId;
    }

    public boolean isAdmin() {
        return currentRole == Role.ADMIN;
    }

    public boolean isStudent() {
        return currentRole == Role.STUDENT;
    }

    public boolean isLoggedIn() {
        return currentRole != null;
    }
}
