package tn.esprit.services;

import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;
import java.sql.*;
import java.time.LocalDateTime;
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
        System.out.println("[UserService] DB connection: " + (cnx != null ? "OK" : "NULL"));
        try {
            if (cnx != null) System.out.println("[UserService] DB closed? " + cnx.isClosed());
        } catch (Exception e) { e.printStackTrace(); }
        ensureTableExists();
        ensureQuickUsersExist();
        ensureQuickAppUsersExist();
        loadUsersFromDb();
        System.out.println("[UserService] Users loaded from DB: " + users.size());
    }

    private void ensureTableExists() {
        String req = "CREATE TABLE IF NOT EXISTS `user` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`username` VARCHAR(100)," +
                "`email` VARCHAR(150) UNIQUE," +
                "`password` VARCHAR(255)," +
                "`role` VARCHAR(20)," +
                "`reset_code` VARCHAR(10)," +
                "`reset_expires_at` TIMESTAMP NULL," +
                "`avatar_style` VARCHAR(50) DEFAULT 'avataaars'," +
                "`address` VARCHAR(255)," +
                "`city` VARCHAR(150)," +
                "`zipcode` VARCHAR(10)" +
                ")";
        try {
            Statement st = cnx.createStatement();
            st.execute(req);
            
            // Migration: Add reset columns if they don't exist
            if (!hasColumn("user", "reset_code")) {
                st.execute("ALTER TABLE `user` ADD COLUMN `reset_code` VARCHAR(10)");
            }
            if (!hasColumn("user", "reset_expires_at")) {
                st.execute("ALTER TABLE `user` ADD COLUMN `reset_expires_at` TIMESTAMP NULL");
            }
            if (!hasColumn("user", "timeout_until")) {
                st.execute("ALTER TABLE `user` ADD COLUMN `timeout_until` DATETIME DEFAULT NULL");
            }
            if (!hasColumn("user", "address")) {
                st.execute("ALTER TABLE `user` ADD COLUMN `address` VARCHAR(255)");
            }
            if (!hasColumn("user", "city")) {
                st.execute("ALTER TABLE `user` ADD COLUMN `city` VARCHAR(150)");
            }
            if (!hasColumn("user", "zipcode")) {
                st.execute("ALTER TABLE `user` ADD COLUMN `zipcode` VARCHAR(10)");
            }
            if (!hasColumn("user", "avatar_style")) {
                st.execute("ALTER TABLE `user` ADD COLUMN `avatar_style` VARCHAR(50) DEFAULT 'avataaars'");
            }
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
                User user = new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getTimestamp("timeout_until") != null ? rs.getTimestamp("timeout_until").toLocalDateTime() : null
                );
                user.setAvatarStyle(rs.getString("avatar_style"));
                user.setAddress(rs.getString("address"));
                user.setCity(rs.getString("city"));
                user.setZipcode(rs.getString("zipcode"));
                users.add(user);
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
                    int id = rs.getInt("id");
                    // Force password/role sync for quick users
                    String updateReq = "UPDATE `user` SET password = ?, role = ? WHERE id = ?";
                    try (PreparedStatement updatePs = cnx.prepareStatement(updateReq)) {
                        updatePs.setString(1, password);
                        updatePs.setString(2, role);
                        updatePs.setInt(3, id);
                        updatePs.executeUpdate();
                    }
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String insertReq = "INSERT INTO `user` (`username`, `email`, `password`, `role`, `avatar_style`) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement insertPs = cnx.prepareStatement(insertReq)) {
            insertPs.setString(1, username);
            insertPs.setString(2, email);
            insertPs.setString(3, password);
            insertPs.setString(4, role);
            insertPs.setString(5, AvatarService.getRandomStyle());
            insertPs.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        loadUsersFromDb();
        System.out.println("[AUTH DEBUG] Attempting login: email='" + email + "' password='" + password + "'");
        System.out.println("[AUTH DEBUG] Total users in DB: " + users.size());
        for (User user : users) {
            System.out.println("[AUTH DEBUG]   DB user: email='" + user.getEmail() + "' password='" + user.getPassword() + "' role=" + user.getRole());
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                System.out.println("[AUTH DEBUG] ✅ MATCH FOUND!");
                return user;
            }
        }
        System.out.println("[AUTH DEBUG] ❌ No match found.");
        return null;
    }

    public User getUserByEmail(String email) {
        if (email == null) return null;
        loadUsersFromDb();
        for (User user : users) {
            if (user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        return null;
    }

    private void ensureQuickAppUsersExist() {
        // Redundant after unification. Both projects now share the 'user' table.
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
        String normalizedRole = normalizeRole(newRole);
        String req = "UPDATE `user` SET role = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, normalizedRole);
            ps.setInt(2, id);
            ps.executeUpdate();

            // Keep in-memory list and active session in sync with DB role changes.
            loadUsersFromDb();
            User updated = findUserById(id);
            if (updated != null) {
                upsertAppUserRoleByEmail(updated.getEmail(), toAppUserRole(normalizedRole));
                User current = SessionManager.getCurrentUser();
                if (current != null && (current.getId() == id || updated.getEmail().equalsIgnoreCase(current.getEmail()))) {
                    current.setRole(normalizedRole);
                    if (current.getUsername() == null || current.getUsername().isEmpty()) {
                        current.setUsername(updated.getUsername());
                    }
                    SessionManager.login(current);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String validateAndRegister(String firstName, String lastName, String email, 
                                     String address, String zipCode, String city, 
                                     String password, String confirmPassword, boolean termsAccepted) {
        email = email == null ? "" : email.trim();
        firstName = firstName == null ? "" : firstName.trim();
        lastName = lastName == null ? "" : lastName.trim();
        address = address == null ? "" : address.trim();
        zipCode = zipCode == null ? "" : zipCode.trim();
        city = city == null ? "" : city.trim();
        password = password == null ? "" : password.trim();
        confirmPassword = confirmPassword == null ? "" : confirmPassword.trim();

        // 1. Basic Fields Presence
        if (email.isEmpty()) return "Please enter your email.";
        if (firstName.isEmpty()) return "Please enter your first name.";
        if (lastName.isEmpty()) return "Please enter your last name.";
        if (address.isEmpty()) return "Please enter your address.";
        if (zipCode.isEmpty()) return "Please enter your postal code.";
        if (city.isEmpty()) return "Please enter your city.";
        if (password.isEmpty()) return "Please enter a password.";
        if (confirmPassword.isEmpty()) return "Please repeat your password.";

        // 2. Email Validation
        if (!EMAIL_PATTERN.matcher(email).matches()) return "Please enter a valid email address.";
        
        // 3. Names Validation
        if (!ALPHA_PATTERN.matcher(firstName).matches()) return "First name can only contain letters and spaces.";
        if (firstName.length() > 100) return "First name cannot be longer than 100 characters.";
        if (!ALPHA_PATTERN.matcher(lastName).matches()) return "Last name can only contain letters and spaces.";
        
        // 4. Address & City Validation
        if (address.length() > 255) return "Address cannot be longer than 255 characters.";
        if (!ZIP_PATTERN.matcher(zipCode).matches()) return "Postal code must be exactly 5 digits.";
        if (!ALPHA_PATTERN.matcher(city).matches()) return "City can only contain letters and spaces.";
        if (city.length() > 150) return "City cannot be longer than 150 characters.";

        // 5. Password Validation
        if (password.length() < 6) return "Your password should be at least 6 characters.";
        if (!password.equals(confirmPassword)) return "The password fields must match.";

        // 6. Terms Validation
        if (!termsAccepted) return "You should agree to our terms.";

        // 7. Duplicate Check
        for (User u : users) {
            if (u.getEmail().equals(email)) return "Email already exists.";
        }

        // 8. Save to DB
        String req = "INSERT INTO `user` (`username`, `email`, `password`, `role`, `avatar_style`, `address`, `city`, `zipcode`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, firstName + " " + lastName);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, "USER");
            ps.setString(5, AvatarService.getRandomStyle());
            ps.setString(6, address);
            ps.setString(7, city);
            ps.setString(8, zipCode);
            ps.executeUpdate();
            upsertAppUserRoleByEmail(email, "ROLE_USER");
            loadUsersFromDb();
            return "SUCCESS";
        } catch (SQLException e) {
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
        String req = "INSERT INTO `user` (`username`, `email`, `password`, `role`, `avatar_style`) VALUES (?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, firstName + " " + lastName);
            ps.setString(2, email);
            ps.setString(3, password);
            ps.setString(4, role);
            ps.setString(5, AvatarService.getRandomStyle());
            ps.executeUpdate();
            upsertAppUserRoleByEmail(email, toAppUserRole(role));
            loadUsersFromDb();
            return "SUCCESS";
        } catch (SQLException e) {
            return "Database Error: " + e.getMessage();
        }
    }

    private User findUserById(int id) {
        for (User u : users) {
            if (u.getId() == id) return u;
        }
        return null;
    }

    private String normalizeRole(String role) {
        if (role == null) return "USER";
        String normalized = role.trim().toUpperCase();
        if ("ADMIN".equals(normalized) || "NGO".equals(normalized) || "USER".equals(normalized)) {
            return normalized;
        }
        return "USER";
    }

    private String toAppUserRole(String userRole) {
        String normalized = normalizeRole(userRole);
        if ("ADMIN".equals(normalized)) return "ROLE_ADMIN";
        if ("NGO".equals(normalized)) return "ROLE_NGO";
        return "ROLE_USER";
    }

    private void upsertAppUserRoleByEmail(String email, String role) {
        // Redundant. Managed via triggers or shared table updates in Symfony/Java entities.
    }

    public boolean setResetCode(String email, String code) {
        String req = "UPDATE `user` SET reset_code = ?, reset_expires_at = DATE_ADD(NOW(), INTERVAL 15 MINUTE) WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, code);
            ps.setString(2, email);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean verifyResetCode(String email, String code) {
        String req = "SELECT 1 FROM `user` WHERE email = ? AND reset_code = ? AND reset_expires_at > NOW()";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            ps.setString(2, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updatePassword(String email, String newPassword) {
        String req = "UPDATE `user` SET password = ?, reset_code = NULL WHERE email = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, newPassword);
            ps.setString(2, email);
            int updated = ps.executeUpdate();
            if (updated > 0) {
                loadUsersFromDb();
                return true;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateTimeout(int id, LocalDateTime until) {
        String req = "UPDATE `user` SET timeout_until = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (until == null) {
                ps.setNull(1, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(1, Timestamp.valueOf(until));
            }
            ps.setInt(2, id);
            ps.executeUpdate();
            loadUsersFromDb();
            
            // Sync with active session if applicable
            tn.esprit.user.User current = SessionManager.getCurrentUser();
            if (current != null && current.getId() == id) {
                current.setTimeoutUntil(until);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateAvatarStyle(int id, String newStyle) {
        if (id < 0) return;
        String req = "UPDATE `user` SET avatar_style = ? WHERE id = ?";
        try {
            PreparedStatement ps = cnx.prepareStatement(req);
            ps.setString(1, newStyle);
            ps.setInt(2, id);
            ps.executeUpdate();
            loadUsersFromDb(); // Refresh cache
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
