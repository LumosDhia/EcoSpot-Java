package tn.esprit.services;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tn.esprit.user.User;
import static org.junit.jupiter.api.Assertions.*;

public class ForgotPasswordTest {

    private static UserService userService;
    private static final String TEST_EMAIL = "test_forgot@mail.com";

    @BeforeAll
    static void setup() {
        userService = new UserService();
        // Ensure test user exists
        userService.validateAndRegister("Test", "Forgot", TEST_EMAIL, "Address", "12345", "City", "password123", "password123", true);
    }

    @Test
    void testForgotPasswordFlow() {
        String code = "123456";
        
        // 1. Set reset code
        boolean setSuccess = userService.setResetCode(TEST_EMAIL, code);
        assertTrue(setSuccess, "Should successfully set reset code");

        // 2. Verify correct code
        boolean verifySuccess = userService.verifyResetCode(TEST_EMAIL, code);
        assertTrue(verifySuccess, "Should successfully verify correct code");

        // 3. Verify incorrect code
        boolean verifyFail = userService.verifyResetCode(TEST_EMAIL, "000000");
        assertFalse(verifyFail, "Should fail verifying incorrect code");

        // 4. Update password
        boolean updateSuccess = userService.updatePassword(TEST_EMAIL, "new_password_123");
        assertTrue(updateSuccess, "Should successfully update password");

        // 5. Authenticate with new password
        User user = userService.authenticate(TEST_EMAIL, "new_password_123");
        assertNotNull(user, "User should be able to login with new password");
        
        // 6. Reset code should be cleared
        boolean verifyAfterReset = userService.verifyResetCode(TEST_EMAIL, code);
        assertFalse(verifyAfterReset, "Reset code should be cleared after password update");
    }
}
