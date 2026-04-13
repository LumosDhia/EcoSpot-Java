package tn.esprit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tn.esprit.services.UserService;
import static org.junit.jupiter.api.Assertions.*;

public class ValidationTests {

    private UserService userService;

    @BeforeEach
    public void setUp() {
        userService = new UserService();
    }

    @Test
    public void testFirstNameValidation() {
        // Invalid First Name (Special characters)
        String res = userService.validateAndRegister("Wiem@", "Jouini", "wiem@test.tn", "", "", "", "pass123", "pass123", true);
        assertEquals("First name can only contain letters and spaces.", res);

        // Invalid First Name (Too long)
        String longName = "a".repeat(101);
        res = userService.validateAndRegister(longName, "Jouini", "wiem@test.tn", "", "", "", "pass123", "pass123", true);
        assertEquals("First name cannot be longer than 100 characters.", res);
    }

    @Test
    public void testZipCodeValidation() {
        // Invalid ZIP (Too short)
        String res = userService.validateAndRegister("Wiem", "Jouini", "wiem@test.tn", "Address", "123", "City", "pass123", "pass123", true);
        assertEquals("Postal code must be exactly 5 digits.", res);

        // Invalid ZIP (Letters)
        res = userService.validateAndRegister("Wiem", "Jouini", "wiem@test.tn", "Address", "abcde", "City", "pass123", "pass123", true);
        assertEquals("Postal code must be exactly 5 digits.", res);
    }

    @Test
    public void testCityValidation() {
        // Invalid City (Special characters)
        String res = userService.validateAndRegister("Wiem", "Jouini", "wiem@test.tn", "Address", "75001", "Paris!", "pass123", "pass123", true);
        assertEquals("City can only contain letters and spaces.", res);

        // Invalid City (Too long)
        String longCity = "a".repeat(151);
        res = userService.validateAndRegister("Wiem", "Jouini", "wiem@test.tn", "Address", "75001", longCity, "pass123", "pass123", true);
        assertEquals("City cannot be longer than 150 characters.", res);
    }

    @Test
    public void testAddressLength() {
        // Invalid Address (Too long)
        String longAddr = "a".repeat(256);
        String res = userService.validateAndRegister("Wiem", "Jouini", "wiem@test.tn", longAddr, "75001", "Paris", "pass123", "pass123", true);
        assertEquals("Address cannot be longer than 255 characters.", res);
    }

    @Test
    public void testTermsNotAccepted() {
        String res = userService.validateAndRegister("Wiem", "Jouini", "wiem@test.tn", "", "", "", "pass123", "pass123", false);
        assertEquals("You should agree to our terms.", res);
    }

    @Test
    public void testValidFullRegistration() {
        String uniqueEmail = "wiemJoe_" + System.currentTimeMillis() + "@test.tn";
        String res = userService.validateAndRegister("Wiem", "Joe", uniqueEmail, "123 Street", "20150", "Tunis", "pass123", "pass123", true);
        assertEquals("SUCCESS", res);
    }
}
