package tn.esprit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.services.UserService;
import static org.junit.jupiter.api.Assertions.*;

public class LoginTests {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testSuccessfulLogin() {
        User user = userService.authenticate("user@mail.com", "user123");
        assertNotNull(user, "User should not be null on successful login");
        assertEquals("Regular User", user.getUsername(), "Username should match");
    }

    @Test
    public void testLoginWithWrongPassword() {
        User user = userService.authenticate("user@mail.com", "wrongpassword");
        assertNull(user, "User should be null for incorrect password");
    }

    @Test
    public void testLoginWithNonExistentEmail() {
        User user = userService.authenticate("nonexistent@ecospot.tn", "password123");
        assertNull(user, "User should be null for non-existent email");
    }

    @Test
    public void testLoginWithNullCredentials() {
        assertNull(userService.authenticate(null, "password123"), "Should handle null email");
        assertNull(userService.authenticate("wiem@ecospot.tn", null), "Should handle null password");
    }

    @Test
    public void testAdminLogin() {
        User user = userService.authenticate("admin@mail.com", "admin123");
        assertNotNull(user, "Admin login should succeed");
        assertEquals("ADMIN", user.getRole(), "Role should be ADMIN");
    }
}
