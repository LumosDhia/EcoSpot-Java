package tn.esprit.ticket;

import org.junit.jupiter.api.*;
import tn.esprit.services.TicketService;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketServiceTest {

    private static TicketService ticketService;
    private static int testTicketId;

    @BeforeAll
    public static void setup() {
        ticketService = new TicketService();
        String appUserEmail = getAnyAppUserEmail();
        assertNotNull(appUserEmail, "Need at least one app_user row for TicketServiceTest.");
        SessionManager.login(new User(1, "TicketServiceTester", appUserEmail, "", "USER"));
    }

    @Test
    @Order(1)
    @DisplayName("User: Create a New Ticket")
    public void testCreateTicket() {
        Ticket t = new Ticket();
        t.setTitle("Test Ticket - Unit Test");
        t.setDescription("This is a detailed description for the unit test. It must be at least 20 characters long.");
        t.setLocation("Tunis, Test City");
        t.setStatus(TicketStatus.PENDING);
        t.setPriority(TicketPriority.MEDIUM);
        t.setDomain(ActionDomain.OTHER);
        t.setUserId(1); // Assuming user 1 exists or just using ID for reference

        ticketService.add(t);
        System.out.println("DEBUG: Ticket added, checking database...");

        // Retrieve to verify and get ID
        List<Ticket> all = ticketService.getAll();
        System.out.println("DEBUG: Found " + all.size() + " total tickets in DB.");
        
        Ticket retrieved = null;
        for (Ticket ticket : all) {
            System.out.println("DEBUG: Comparing with ticket title: [" + ticket.getTitle() + "]");
            if ("Test Ticket - Unit Test".equals(ticket.getTitle())) {
                retrieved = ticket;
                break;
            }
        }

        assertNotNull(retrieved, "Ticket should have been saved in the database. Total tickets found: " + all.size());
        testTicketId = retrieved.getId();
        assertEquals(TicketStatus.PENDING, retrieved.getStatus(), "New tickets should start as PENDING.");
    }

    @Test
    @Order(2)
    @DisplayName("Admin: Send Ticket Back for Revision with Comment")
    public void testReturnTicketForRevision() {
        Ticket t = ticketService.getAll().stream()
                .filter(ticket -> ticket.getId() == testTicketId)
                .findFirst()
                .orElse(null);

        assertNotNull(t);
        
        String adminComment = "Please provide more details about the exact location.";
        t.setStatus(TicketStatus.SENT_BACK);
        t.setAdminNotes(adminComment);

        ticketService.update(t);

        // Re-fetch
        Ticket updated = ticketService.getAll().stream()
                .filter(ticket -> ticket.getId() == testTicketId)
                .findFirst()
                .orElse(null);

        assertEquals(TicketStatus.SENT_BACK, updated.getStatus());
        assertEquals(adminComment, updated.getAdminNotes());
    }

    @Test
    @Order(3)
    @DisplayName("Admin: Publish Ticket after review")
    public void testPublishTicket() {
        Ticket t = ticketService.getAll().stream()
                .filter(ticket -> ticket.getId() == testTicketId)
                .findFirst()
                .orElse(null);

        assertNotNull(t);
        
        t.setStatus(TicketStatus.PUBLISHED);
        ticketService.update(t);

        // Re-fetch
        Ticket updated = ticketService.getAll().stream()
                .filter(ticket -> ticket.getId() == testTicketId)
                .findFirst()
                .orElse(null);

        assertEquals(TicketStatus.PUBLISHED, updated.getStatus());
    }

    @Test
    @Order(4)
    @DisplayName("Admin: Refuse Ticket")
    public void testRefuseTicket() {
        Ticket t = ticketService.getAll().stream()
                .filter(ticket -> ticket.getId() == testTicketId)
                .findFirst()
                .orElse(null);

        assertNotNull(t);
        
        t.setStatus(TicketStatus.REFUSED);
        ticketService.update(t);

        // Re-fetch
        Ticket updated = ticketService.getAll().stream()
                .filter(ticket -> ticket.getId() == testTicketId)
                .findFirst()
                .orElse(null);

        assertEquals(TicketStatus.REFUSED, updated.getStatus());
    }

    @AfterAll
    public static void cleanup() {
        if (testTicketId > 0) {
            Ticket dummy = new Ticket();
            dummy.setId(testTicketId);
            ticketService.delete(dummy);
            System.out.println("Cleaned up unit test ticket id: " + testTicketId);
        }
        SessionManager.logout();
    }

    private static String getAnyAppUserEmail() {
        Connection cnx = MyConnection.getInstance().getCnx();
        if (cnx == null) return null;
        String req = "SELECT email FROM app_user LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("email");
        } catch (SQLException ignored) {
        }
        return null;
    }
}
