package tn.esprit.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tn.esprit.services.EventService;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EventParticipationFlowTest {

    private final EventService eventService = new EventService();
    private Integer createdEventId;
    private Integer createdUserId;

    @AfterEach
    void cleanup() {
        Connection cnx = MyConnection.getInstance().getCnx();
        if (cnx == null) {
            return;
        }
        try {
            if (createdEventId != null) {
                try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM event_participant WHERE event_id = ?")) {
                    ps.setInt(1, createdEventId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM event WHERE id = ?")) {
                    ps.setInt(1, createdEventId);
                    ps.executeUpdate();
                }
            }
            if (createdUserId != null) {
                try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM `user` WHERE id = ?")) {
                    ps.setInt(1, createdUserId);
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            fail("Cleanup failed: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Join Event adds user to participants list and prevents duplicates")
    void joinEventPersistsParticipantAndRejectsDuplicateJoin() {
        Event event = new Event();
        String suffix = String.valueOf(System.currentTimeMillis());
        event.setName("Participation Test " + suffix);
        event.setSlug("participation-test-" + suffix);
        event.setDescription("Integration test event for participant join flow.");
        event.setCapacity(100);
        event.setLocation("Test Venue");
        event.setStartedAt(LocalDateTime.now().plusDays(3));
        event.setEndedAt(LocalDateTime.now().plusDays(4));
        event.setImage("");
        eventService.add(event);
        assertTrue(event.getId() > 0, "Event should be created in DB");
        createdEventId = event.getId();

        User testUser = createTestUser("participant+" + suffix + "@mail.com");
        createdUserId = testUser.getId();
        assertTrue(createdUserId > 0, "User should be created in DB");

        boolean firstJoin = eventService.joinEvent(createdEventId, createdUserId);
        assertTrue(firstJoin, "First join should insert participant row");
        assertTrue(eventService.isUserParticipant(createdEventId, createdUserId), "User should be marked as participant");

        boolean secondJoin = eventService.joinEvent(createdEventId, createdUserId);
        assertFalse(secondJoin, "Second join should be ignored as duplicate");

        List<User> participants = eventService.getParticipantsForEvent(createdEventId);
        assertFalse(participants.isEmpty(), "Participants list should not be empty");
        assertTrue(participants.stream().anyMatch(u -> u.getId() == createdUserId), "Participants list should include joined user");
    }

    private User createTestUser(String email) {
        Connection cnx = MyConnection.getInstance().getCnx();
        assertNotNull(cnx, "Database connection should be available for tests");

        String username = "Participant " + System.currentTimeMillis();
        String req = "INSERT INTO `user` (`username`, `email`, `password`, `role`) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, "123456");
            ps.setString(4, "USER");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new User(keys.getInt(1), username, email, "", "USER");
                }
            }
        } catch (SQLException e) {
            fail("Failed to create test user: " + e.getMessage());
        }
        throw new IllegalStateException("Unable to create test user");
    }
}
