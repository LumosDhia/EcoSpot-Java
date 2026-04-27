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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDashboardFlowsTest {

    private static TicketService ticketService;
    private static final List<Integer> createdTicketIds = new ArrayList<>();

    @BeforeAll
    static void setup() {
        ticketService = new TicketService();
        String appUserEmail = getAnyAppUserEmail();
        assertNotNull(appUserEmail, "app_user table should contain at least one user for dashboard tests.");
        SessionManager.login(new User(1, "DashboardTester", appUserEmail, "", "USER"));
    }

    @AfterAll
    static void cleanup() {
        for (Integer id : createdTicketIds) {
            Ticket t = new Ticket();
            t.setId(id);
            ticketService.delete(t);
        }
        SessionManager.logout();
    }

    @Test
    @Order(1)
    @DisplayName("User dashboard: My tickets list returns user-created ticket")
    void testMyTicketsQueryForCurrentUser() {
        Ticket created = createBaseTicket("Dashboard MyTickets");
        List<Ticket> mine = ticketService.getByUserId(1);
        assertTrue(
                mine.stream().anyMatch(t -> t.getId() == created.getId()),
                "Created ticket must appear in My Tickets data source."
        );
    }

    @Test
    @Order(2)
    @DisplayName("User dashboard: Published ticket appears in community list")
    void testCommunityTicketsVisibility() {
        Ticket created = createBaseTicket("Dashboard Community");
        created.setStatus(TicketStatus.PUBLISHED);
        ticketService.update(created);

        List<Ticket> communityVisible = ticketService.getAll().stream()
                .filter(t ->
                        t.getStatus() == TicketStatus.PUBLISHED
                                || t.getStatus() == TicketStatus.ASSIGNED
                                || t.getStatus() == TicketStatus.IN_PROGRESS
                                || t.getStatus() == TicketStatus.COMPLETED
                )
                .toList();

        assertTrue(
                communityVisible.stream().anyMatch(t -> t.getId() == created.getId()),
                "Published ticket should be visible in community tickets."
        );
    }

    @Test
    @Order(3)
    @DisplayName("User dashboard: Revision cycle returns ticket to pending")
    void testRevisionCycleBackToPending() {
        Ticket created = createBaseTicket("Dashboard Revision");
        created.setStatus(TicketStatus.SENT_BACK);
        created.setAdminNotes("Please revise location details.");
        ticketService.update(created);

        Ticket sentBack = ticketService.getById(created.getId());
        assertNotNull(sentBack);
        assertEquals(TicketStatus.SENT_BACK, sentBack.getStatus());
        assertNotNull(sentBack.getAdminNotes());

        // Simulate user editing + resubmitting
        sentBack.setDescription(sentBack.getDescription() + " (updated)");
        sentBack.setStatus(TicketStatus.PENDING);
        sentBack.setAdminNotes(null);
        ticketService.update(sentBack);

        Ticket resubmitted = ticketService.getById(created.getId());
        assertNotNull(resubmitted);
        assertEquals(TicketStatus.PENDING, resubmitted.getStatus());
        assertNull(resubmitted.getAdminNotes(), "Admin notes should be cleared on resubmission.");
    }

    @Test
    @Order(4)
    @DisplayName("User dashboard: Completed ticket appears in achievements")
    void testAchievementsVisibility() {
        Ticket created = createBaseTicket("Dashboard Achievement");
        created.setStatus(TicketStatus.COMPLETED);
        created.setAchievedAt(LocalDateTime.now());
        ticketService.update(created);

        List<Ticket> achievements = ticketService.getAll().stream()
                .filter(t -> t.getStatus() == TicketStatus.COMPLETED)
                .toList();

        assertTrue(
                achievements.stream().anyMatch(t -> t.getId() == created.getId()),
                "Completed ticket should appear in achievements source."
        );
    }

    private Ticket createBaseTicket(String prefix) {
        String uniqueTitle = prefix + " " + System.currentTimeMillis();
        Ticket t = new Ticket();
        t.setTitle(uniqueTitle);
        t.setDescription("This ticket is created by automated dashboard lifecycle test.");
        t.setLocation("Test Location");
        t.setStatus(TicketStatus.PENDING);
        t.setPriority(TicketPriority.MEDIUM);
        t.setDomain(ActionDomain.OTHER);
        t.setUserId(1);
        ticketService.add(t);

        Ticket created = ticketService.getAll().stream()
                .filter(x -> uniqueTitle.equals(x.getTitle()))
                .findFirst()
                .orElse(null);
        assertNotNull(created, "Ticket should be persisted and retrievable.");
        createdTicketIds.add(created.getId());
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

