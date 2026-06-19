package com.campuslf.service;

import com.campuslf.dao.AdminDAO;
import com.campuslf.models.Admin;

public class AuthenticationService {

    private final AdminDAO adminDAO;

    public AuthenticationService() {
        this.adminDAO = new AdminDAO();
    }

    public boolean login(String username, String password) {

        Admin admin =
                adminDAO.getAdminByUsername(username);

        if (admin == null) {

            return false;

        } else {

            return admin.getPassword().equals(password);
        }
    }

    public boolean usernameExists(String username) {
        return adminDAO.usernameExists(username);
    }

    public boolean createAdmin(String username, String password) {
        if (usernameExists(username)) {
            return false;
        }

        Admin admin = new Admin();
        admin.setUsername(username);
        admin.setPassword(password);
        return adminDAO.addAdmin(admin);
    }
}