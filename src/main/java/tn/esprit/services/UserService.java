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
        users.add(new User(-1, "Admin User", "admin@mail.com", "admin123", "ADMIN"));
        users.add(new User(-2, "NGO Organization", "ngo@mail.com", "ngo123", "NGO"));
        users.add(new User(-3, "Regular User", "user@mail.com", "user123", "USER"));

        String req = "SELECT * FROM `user` WHERE email NOT IN ('admin@mail.com', 'ngo@mail.com', 'user@mail.com')";
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

    public User authenticate(String email, String password) {
        if (email == null || password == null) return null;
        for (User user : users) {
            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
                return user;
            }
        }
        return null;
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
