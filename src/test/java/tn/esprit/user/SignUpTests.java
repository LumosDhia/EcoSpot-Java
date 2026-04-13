package tn.esprit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.services.UserService;
import static org.junit.jupiter.api.Assertions.*;

public class SignUpTests {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testSuccessfulSignUp() {
        String uniqueEmail = "new_" + System.currentTimeMillis() + "@mail.com";
        String result = userService.validateAndRegister("New", "User", uniqueEmail, "", "", "", "password123", "password123", true);
        assertEquals("SUCCESS", result, "Registration should succeed with valid data");
    }

    @Test
    public void testSignUpWithExistingEmail() {
        String result = userService.validateAndRegister("Wiem", "Jouini", "admin@mail.com", "", "", "", "password123", "password123", true);
        assertEquals("Email already exists.", result, "Should not allow duplicate emails");
    }

    @Test
    public void testSignUpWithInvalidEmail() {
        String result = userService.validateAndRegister("User", "Name", "invalid-email", "", "", "", "password123", "password123", true);
        assertEquals("Please enter a valid email address.", result, "Should reject malformed emails");
    }

    @Test
    public void testSignUpWithShortPassword() {
        String result = userService.validateAndRegister("User", "Name", "user@test.tn", "", "", "", "123", "123", true);
        assertEquals("Your password should be at least 6 characters.", result, "Should enforce password length");
    }

    @Test
    public void testSignUpWithPasswordMismatch() {
        String result = userService.validateAndRegister("User", "Name", "user@test.tn", "", "", "", "password123", "mismatch123", true);
        assertEquals("The password fields must match.", result, "Should verify password confirmation");
    }

    @Test
    public void testSignUpWithMissingUsername() {
        String result = userService.validateAndRegister("", "Name", "user@test.tn", "", "", "", "password123", "password123", true);
        assertEquals("Please enter your first name.", result, "Should require a first name");
    }
}
