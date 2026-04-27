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

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TicketCompletionReviewFlowTest {

    private static TicketService ticketService;
    private static Integer completionTicketId;
    private static Integer rejectedCompletionTicketId;

    @BeforeAll
    static void setup() {
        ticketService = new TicketService();
        String appUserEmail = getAnyAppUserEmail();
        assertNotNull(appUserEmail, "Need at least one app_user row for completion-flow tests.");
        SessionManager.login(new User(1, "CompletionTester", appUserEmail, "", "USER"));
    }

    @AfterAll
    static void cleanup() {
        if (completionTicketId != null) {
            Ticket t = new Ticket();
            t.setId(completionTicketId);
            ticketService.delete(t);
        }
        if (rejectedCompletionTicketId != null) {
            Ticket t = new Ticket();
            t.setId(rejectedCompletionTicketId);
            ticketService.delete(t);
        }
        SessionManager.logout();
    }

    @Test
    @Order(1)
    @DisplayName("User submits completion proof -> IN_PROGRESS")
    void testSubmitCompletionProof() {
        Ticket t = createPublishedTicket("Completion review");
        t.setCompletionMessage("I cleaned and fixed this area with before/after proof.");
        t.setCompletionImage("file:///tmp/proof-completion.jpg");
        t.setStatus(TicketStatus.IN_PROGRESS);
        ticketService.update(t);

        Ticket reloaded = ticketService.getById(t.getId());
        assertNotNull(reloaded);
        assertEquals(TicketStatus.IN_PROGRESS, reloaded.getStatus());
        assertNotNull(reloaded.getCompletionMessage());
        assertNotNull(reloaded.getCompletionImage());
        completionTicketId = reloaded.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Admin accepts completion -> COMPLETED")
    void testAdminAcceptsCompletion() {
        assertNotNull(completionTicketId);
        Ticket t = ticketService.getById(completionTicketId);
        assertNotNull(t);

        t.setStatus(TicketStatus.COMPLETED);
        t.setAchievedAt(java.time.LocalDateTime.now());
        ticketService.update(t);

        Ticket reloaded = ticketService.getById(completionTicketId);
        assertNotNull(reloaded);
        assertEquals(TicketStatus.COMPLETED, reloaded.getStatus());
        assertNotNull(reloaded.getAchievedAt(), "Accepted completion should set achieved_at.");
    }

    @Test
    @Order(3)
    @DisplayName("Admin rejects completion -> back to PUBLISHED")
    void testAdminRejectsCompletion() {
        Ticket t = createPublishedTicket("Completion rejected");
        t.setCompletionMessage("Completion proof pending check");
        t.setCompletionImage("file:///tmp/proof-rejected.jpg");
        t.setStatus(TicketStatus.IN_PROGRESS);
        ticketService.update(t);

        // Reject path used by controller: keep in tickets
        t.setStatus(TicketStatus.PUBLISHED);
        t.setAchievedAt(null);
        t.setCompletionMessage(null);
        t.setCompletionImage(null);
        ticketService.update(t);

        Ticket reloaded = ticketService.getById(t.getId());
        assertNotNull(reloaded);
        assertEquals(TicketStatus.PUBLISHED, reloaded.getStatus());
        assertNull(reloaded.getCompletionMessage());
        assertNull(reloaded.getCompletionImage());
        rejectedCompletionTicketId = reloaded.getId();
    }

    private Ticket createPublishedTicket(String prefix) {
        String uniqueTitle = prefix + " " + System.currentTimeMillis();
        Ticket t = new Ticket();
        t.setTitle(uniqueTitle);
        t.setDescription("Ticket created for completion review flow tests.");
        t.setLocation("Test Completion Zone");
        t.setStatus(TicketStatus.PUBLISHED);
        t.setPriority(TicketPriority.MEDIUM);
        t.setDomain(ActionDomain.OTHER);
        t.setUserId(1);
        ticketService.add(t);

        Ticket created = ticketService.getAll().stream()
                .filter(x -> uniqueTitle.equals(x.getTitle()))
                .findFirst()
                .orElse(null);
        assertNotNull(created, "Ticket must be inserted for completion review test.");
        return created;
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

