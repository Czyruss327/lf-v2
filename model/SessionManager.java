package model;

public class SessionManager {

    public enum Role {
        ADMIN, STUDENT
    }

    private static SessionManager instance;
    private Role currentRole;
    private String currentUsername;

    private SessionManager() {
    }

    public static SessionManager getInstance() {
        if (instance == null)
            instance = new SessionManager();
        return instance;
    }

    public void login(Role role, String username) {
        this.currentRole = role;
        this.currentUsername = username;
    }

    public void logout() {
        this.currentRole = null;
        this.currentUsername = null;
    }

    public Role getRole() {
        return currentRole;
    }

    public String getUsername() {
        return currentUsername;
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
