package tn.esprit.services;

import org.junit.jupiter.api.*;
import tn.esprit.user.User;

import java.util.List;

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

    @Test
    @Order(7)
    public void testAdminCanCreateUserInDatabase() {
        String uniqueEmail = "admin_create_" + System.currentTimeMillis() + "@example.com";
        String result = userService.validateAndRegisterAdmin(
                "Admin",
                "Created",
                uniqueEmail,
                "USER",
                "pass123",
                "pass123"
        );
        assertEquals("SUCCESS", result, "Admin user creation should persist in DB");

        User created = userService.authenticate(uniqueEmail, "pass123");
        assertNotNull(created, "Created user must be readable from DB/auth flow");
        assertEquals("USER", created.getRole(), "Created user role should match");
    }

    @Test
    @Order(8)
    public void testAdminCanPromoteAndDemoteUserRoleInDatabase() {
        String uniqueEmail = "role_change_" + System.currentTimeMillis() + "@example.com";
        String createResult = userService.validateAndRegisterAdmin(
                "Role",
                "Tester",
                uniqueEmail,
                "USER",
                "pass123",
                "pass123"
        );
        assertEquals("SUCCESS", createResult);

        User created = userService.authenticate(uniqueEmail, "pass123");
        assertNotNull(created);

        userService.updateUserRoleDirectly(created.getId(), "NGO");
        User promoted = userService.authenticate(uniqueEmail, "pass123");
        assertNotNull(promoted);
        assertEquals("NGO", promoted.getRole(), "Role should be updated to NGO in DB");

        userService.updateUserRoleDirectly(created.getId(), "ADMIN");
        User promotedAgain = userService.authenticate(uniqueEmail, "pass123");
        assertNotNull(promotedAgain);
        assertEquals("ADMIN", promotedAgain.getRole(), "Role should be updated to ADMIN in DB");
    }

    @Test
    @Order(9)
    public void testAdminCanDeleteUserInDatabase() {
        String uniqueEmail = "delete_user_" + System.currentTimeMillis() + "@example.com";
        String createResult = userService.validateAndRegisterAdmin(
                "Delete",
                "Tester",
                uniqueEmail,
                "USER",
                "pass123",
                "pass123"
        );
        assertEquals("SUCCESS", createResult);

        User created = userService.authenticate(uniqueEmail, "pass123");
        assertNotNull(created);

        userService.removeUser(created.getId());
        User deleted = userService.authenticate(uniqueEmail, "pass123");
        assertNull(deleted, "Deleted user should no longer authenticate");
    }

    @Test
    @Order(10)
    public void testUserManagementListReadsFromDatabase() {
        String uniqueEmail = "list_user_" + System.currentTimeMillis() + "@example.com";
        String createResult = userService.validateAndRegisterAdmin(
                "List",
                "Tester",
                uniqueEmail,
                "USER",
                "pass123",
                "pass123"
        );
        assertEquals("SUCCESS", createResult);

        List<User> users = userService.getAllUsers();
        boolean found = users.stream().anyMatch(u -> uniqueEmail.equalsIgnoreCase(u.getEmail()));
        assertTrue(found, "getAllUsers should include newly inserted DB user");
    }
}
