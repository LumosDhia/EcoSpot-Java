package tn.esprit.services;

import tn.esprit.user.User;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class UserService {
    private List<User> users = new ArrayList<>();
    
    // Regex Patterns (Unified from Web Project)
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ALPHA_PATTERN = Pattern.compile("^[\\p{L}\\s\\'-]+$");
    private static final Pattern ZIP_PATTERN = Pattern.compile("^\\d{5}$");

    public UserService() {
        // Mock data
        users.add(new User(1, "Admin", "admin@mail.com", "admin123", "ADMIN"));
        users.add(new User(2, "NGO Organization", "ngo@mail.com", "ngo123", "NGO"));
        users.add(new User(3, "Regular User", "user@mail.com", "user123", "USER"));
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

    public String validateAndRegister(String firstName, String lastName, String email, 
                                     String address, String zipCode, String city, 
                                     String password, String confirmPassword, boolean termsAccepted) {
        
        // Email Validation
        if (email == null || email.isEmpty()) return "Please enter your email.";
        if (!EMAIL_PATTERN.matcher(email).matches()) return "Please enter a valid email address.";
        for (User u : users) {
            if (u.getEmail().equals(email)) return "Email already exists.";
        }

        // Names Validation
        if (firstName == null || firstName.isEmpty()) return "Please enter your first name.";
        if (!ALPHA_PATTERN.matcher(firstName).matches()) return "First name can only contain letters and spaces.";
        if (firstName.length() > 100) return "First name cannot be longer than 100 characters.";

        if (lastName == null || lastName.isEmpty()) return "Please enter your last name.";
        if (!ALPHA_PATTERN.matcher(lastName).matches()) return "Last name can only contain letters and spaces.";
        if (lastName.length() > 100) return "Last name cannot be longer than 100 characters.";

        // Address Validation
        if (address != null && address.length() > 255) return "Address cannot be longer than 255 characters.";

        // ZIP Code Validation
        if (zipCode != null && !zipCode.isEmpty()) {
            if (!ZIP_PATTERN.matcher(zipCode).matches()) return "Postal code must be exactly 5 digits.";
        }

        // City Validation
        if (city != null && !city.isEmpty()) {
            if (!ALPHA_PATTERN.matcher(city).matches()) return "City can only contain letters and spaces.";
            if (city.length() > 150) return "City cannot be longer than 150 characters.";
        }

        // Password Validation
        if (password == null || password.isEmpty()) return "Please enter a password.";
        if (password.length() < 6) return "Your password should be at least 6 characters.";
        if (!password.equals(confirmPassword)) return "The password fields must match.";

        // Terms Validation
        if (!termsAccepted) return "You should agree to our terms.";

        // Registration Success
        users.add(new User(users.size() + 1, firstName + " " + lastName, email, password, "USER"));
        return "SUCCESS";
    }

    public List<User> getAllUsers() {
        return users;
    }
}
