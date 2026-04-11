package tn.esprit.services;

import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserService {
    private static List<User> users = new ArrayList<>();
    private Connection cnx;
    
    // Regex Patterns (Unified from Project Standards)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("^[\\p{L}\\s\\'-]+$");
    private static final Pattern ZIP_PATTERN = Pattern.compile("^\\d{5}$");

    public UserService() {
        cnx = MyConnection.getInstance().getCnx();
        ensureTableExists();
        ensureQuickUsersExist();
        ensureQuickAppUsersExist();
        loadUsersFromDb();
    }

    private void ensureTableExists() {
        String req = "CREATE TABLE IF NOT EXISTS `user` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`username` VARCHAR(100)," +
                "`email` VARCHAR(150) UNIQUE," +
                "`password` VARCHAR(255)," +
                "`role` VARCHAR(20)" +
                ")";
        try {
            Statement st = cnx.createStatement();
            st.execute(req);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadUsersFromDb() {
        users.clear();

        String req = "SELECT * FROM `user`";
        try {
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(req);
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureQuickUsersExist() {
        ensureQuickUser("Admin User", "admin@mail.com", "admin123", "ADMIN");
        ensureQuickUser("NGO Organization", "ngo@mail.com", "ngo123", "NGO");
        ensureQuickUser("Regular User", "user@mail.com", "user123", "USER");
    }

    private void ensureQuickUser(String username, String email, String password, String role) {
        String selectReq = "SELECT id FROM `user` WHERE email = ?";
        try (PreparedStatement selectPs = cnx.prepareStatement(selectReq)) {
            selectPs.setString(1, email);
            try (ResultSet rs = selectPs.executeQuery()) {
                if (rs.next()) {
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String insertReq = "INSERT INTO `user` (`username`, `email`, `password`, `role`) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertPs = cnx.prepareStatement(insertReq)) {
            insertPs.setString(1, username);
            insertPs.setString(2, email);
            insertPs.setString(3, password);
            insertPs.setString(4, role);
            insertPs.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        loadUsersFromDb();
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
    }

    private void ensureQuickAppUsersExist() {
        if (!hasTable("app_user")) return;
        ensureQuickAppUser("admin@mail.com", "Admin", "User", "ROLE_ADMIN");
        ensureQuickAppUser("ngo@mail.com", "NGO", "Organization", "ROLE_NGO");
        ensureQuickAppUser("user@mail.com", "Regular", "User", "ROLE_USER");

        ensureQuickAppUser("admin@ecospot.local", "Admin", "User", "ROLE_ADMIN");
        ensureQuickAppUser("ngo@ecospot.local", "NGO", "Organization", "ROLE_NGO");
        ensureQuickAppUser("user@ecospot.local", "Regular", "User", "ROLE_USER");
    }

    private void ensureQuickAppUser(String email, String firstName, String lastName, String role) {
        String selectReq = "SELECT 1 FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(selectReq)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return;
            }
        } catch (SQLException e) {
            return;
        }

        boolean hasId = hasColumn("app_user", "id");
        boolean hasEmail = hasColumn("app_user", "email");
        boolean hasPassword = hasColumn("app_user", "password");
        boolean hasRoles = hasColumn("app_user", "roles");
        boolean hasFirstname = hasColumn("app_user", "firstname");
        boolean hasLastname = hasColumn("app_user", "lastname");
        boolean hasIsVerified = hasColumn("app_user", "is_verified");
        boolean hasEnabled = hasColumn("app_user", "enabled");
        boolean hasCreatedAt = hasColumn("app_user", "created_at");
        boolean hasUpdatedAt = hasColumn("app_user", "updated_at");

        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        List<Object> params = new ArrayList<>();

        if (hasId) {
            appendColumn(columns, values, "id", "UUID_TO_BIN(UUID())");
        }
        if (hasEmail) {
            appendColumn(columns, values, "email", "?");
            params.add(email);
        }
        if (hasPassword) {
            appendColumn(columns, values, "password", "?");
            params.add("123456");
        }
        if (hasRoles) {
            appendColumn(columns, values, "roles", "?");
            params.add(role);
        }
        if (hasFirstname) {
            appendColumn(columns, values, "firstname", "?");
            params.add(firstName);
        }
        if (hasLastname) {
            appendColumn(columns, values, "lastname", "?");
            params.add(lastName);
        }
        if (hasIsVerified) {
            appendColumn(columns, values, "is_verified", "?");
            params.add(Boolean.TRUE);
        }
        if (hasEnabled) {
            appendColumn(columns, values, "enabled", "?");
            params.add(Boolean.TRUE);
        }
        if (hasCreatedAt) {
            appendColumn(columns, values, "created_at", "NOW()");
        }
        if (hasUpdatedAt) {
            appendColumn(columns, values, "updated_at", "NOW()");
        }

        if (columns.length() == 0) return;

        String req = "INSERT INTO app_user (" + columns + ") VALUES (" + values + ")";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            int i = 1;
            for (Object param : params) {
                if (param instanceof Boolean) {
                    ps.setBoolean(i++, (Boolean) param);
                } else {
                    ps.setString(i++, String.valueOf(param));
                }
            }
            ps.executeUpdate();
        } catch (SQLException ignored) {
        }
    }

    private void appendColumn(StringBuilder columns, StringBuilder values, String column, String valueExpr) {
        if (columns.length() > 0) {
            columns.append(", ");
            values.append(", ");
        }
        columns.append(column);
        values.append(valueExpr);
    }

    private boolean hasTable(String tableName) {
        String req = "SELECT 1 FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean hasColumn(String tableName, String columnName) {
        String req = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<User> getAllUsers() {
        loadUsersFromDb();
        return users;
    }

    public void removeUser(int id) {
        if (id < 0) return;
        String req = "DELETE FROM `user` WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void promoteUser(int id) {
        updateRole(id, true);
    }

    public void demoteUser(int id) {
        updateRole(id, false);
    }

    private void updateRole(int id, boolean promote) {
        if (id < 0) return;
        User user = null;
        for (User u : users) {
             if (u.getId() == id) { user = u; break; }
        }
        if (user == null) return;

        String oldRole = user.getRole();
        String newRole = oldRole;

        if (promote) {
            if ("USER".equals(oldRole)) newRole = "NGO";
            else if ("NGO".equals(oldRole)) newRole = "ADMIN";
        } else {
            if ("ADMIN".equals(oldRole)) newRole = "NGO";
            else if ("NGO".equals(oldRole)) newRole = "USER";
        }

        updateUserRoleDirectly(id, newRole);
    }

    public void updateUserRoleDirectly(int id, String newRole) {
        if (id < 0) return;
        String req = "UPDATE `user` SET role = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, newRole);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String validateAndRegister(String firstName, String lastName, String email, 
                                     String address, String zipCode, String city, 
                                     String password, String confirmPassword, boolean termsAccepted) {
        
        // 1. Basic Fields Presence
        if (email == null || email.isEmpty()) return "Please enter your email.";
        if (firstName == null || firstName.isEmpty()) return "Please enter your first name.";
        if (lastName == null || lastName.isEmpty()) return "Please enter your last name.";

        // 2. Email Validation
        if (!EMAIL_PATTERN.matcher(email).matches()) return "Please enter a valid email address.";
        
        // 3. Names Validation
        if (!ALPHA_PATTERN.matcher(firstName).matches()) return "First name can only contain letters and spaces.";
        if (firstName.length() > 100) return "First name cannot be longer than 100 characters.";
        if (!ALPHA_PATTERN.matcher(lastName).matches()) return "Last name can only contain letters and spaces.";
        
        // 4. Address & City Validation
        if (address != null && address.length() > 255) return "Address cannot be longer than 255 characters.";
        if (zipCode != null && !zipCode.isEmpty()) {
            if (!ZIP_PATTERN.matcher(zipCode).matches()) return "Postal code must be exactly 5 digits.";
        }
        if (city != null && !city.isEmpty()) {
            if (!ALPHA_PATTERN.matcher(city).matches()) return "City can only contain letters and spaces.";
            if (city.length() > 150) return "City cannot be longer than 150 characters.";
        }

        // 5. Password Validation
        if (password == null || password.isEmpty()) return "Please enter a password.";
        if (password.length() < 6) return "Your password should be at least 6 characters.";
        if (!password.equals(confirmPassword)) return "The password fields must match.";

        // 6. Terms Validation
        if (!termsAccepted) return "You should agree to our terms.";

        // 7. Duplicate Check (Check LAST so other validations run first for unit tests)
        for (User u : users) {
            if (u.getEmail().equals(email)) return "Email already exists.";
        }

        // 8. Save to DB
        String req = "INSERT INTO `user` (`username`, `email`, `password`, `role`) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, firstName + " " + lastName);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, "USER");
            ps.executeUpdate();
            return "SUCCESS";
        } catch (SQLException e) {
            // e.printStackTrace();
            return "Database Error: " + e.getMessage();
        }
    }

    public String validateAndRegisterAdmin(String firstName, String lastName, String email, 
                                           String role, String password, String confirmPassword) {
        
        // 1. Basic Fields Presence
        if (email == null || email.isEmpty()) return "Please enter an email.";
        if (firstName == null || firstName.isEmpty()) return "Please enter a first name.";
        if (lastName == null || lastName.isEmpty()) return "Please enter a last name.";

        // 2. Email Validation
        if (!EMAIL_PATTERN.matcher(email).matches()) return "Please enter a valid email address.";
        
        // 3. Names Validation
        if (!ALPHA_PATTERN.matcher(firstName).matches()) return "First name can only contain letters and spaces.";
        if (firstName.length() > 100) return "First name cannot be longer than 100 characters.";
        if (!ALPHA_PATTERN.matcher(lastName).matches()) return "Last name can only contain letters and spaces.";
        
        // 4. Role Validation
        if (role == null || (!role.equals("ADMIN") && !role.equals("NGO") && !role.equals("USER"))) {
            return "Please select a valid user type.";
        }

        // 5. Password Validation
        if (password == null || password.isEmpty()) return "Please enter a password.";
        if (password.length() < 6) return "Password should be at least 6 characters.";
        if (!password.equals(confirmPassword)) return "The password fields must match.";

        // 6. Duplicate Check
        for (User u : users) {
            if (u.getEmail().equals(email)) return "Email already exists in system.";
        }

        // 7. Save to DB
        String req = "INSERT INTO `user` (`username`, `email`, `password`, `role`) VALUES (?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, firstName + " " + lastName);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.executeUpdate();
            return "SUCCESS";
        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
    }
}
