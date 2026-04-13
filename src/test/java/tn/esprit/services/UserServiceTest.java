package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.user.User;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest {

    private static UserService userService;

    @BeforeAll
    public static void setUp() {
        userService = new UserService();
    }

    @Test
    @Order(1)
    public void testHardcodedAdminLogin() {
        User user = userService.authenticate("admin@mail.com", "admin123");
        assertNotNull(user, "Hardcoded Admin should be able to login");
        assertEquals("ADMIN", user.getRole(), "Role should be ADMIN");
    }

    @Test
    @Order(2)
    public void testFailedLogin() {
        User user = userService.authenticate("wrong@mail.com", "wrongpass");
        assertNull(user, "Invalid credentials should return null");
    }

    @Test
    @Order(3)
    public void testEmailValidation() {
        String result = userService.validateAndRegister("Test", "User", "invalid-email", 
                                                        null, null, null, 
                                                        "pass123", "pass123", true);
        assertEquals("Please enter a valid email address.", result);
    }

    @Test
    @Order(4)
    public void testPasswordMismatch() {
        String result = userService.validateAndRegister("Test", "User", "valid@test.com", 
                                                        null, null, null, 
                                                        "password123", "different", true);
        assertEquals("The password fields must match.", result);
    }

    @Test
    @Order(5)
    public void testTermsNotAccepted() {
        String result = userService.validateAndRegister("Test", "User", "valid@test.com", 
                                                        null, null, null, 
                                                        "password123", "password123", false);
        assertEquals("You should agree to our terms.", result);
    }

    @Test
    @Order(6)
    public void testSuccessfulValidation() {
        String uniqueEmail = "testuser_" + System.currentTimeMillis() + "@example.com";
        String result = userService.validateAndRegister("Unit", "Test", uniqueEmail, 
                                                        null, null, null, 
                                                        "testpass123", "testpass123", true);
        assertEquals("SUCCESS", result, "Valid data should return SUCCESS");
    }
}
