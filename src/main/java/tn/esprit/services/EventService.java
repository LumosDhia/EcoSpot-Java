package tn.esprit.services;

import tn.esprit.event.Event;
import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.user.User;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements GlobalInterface<Event> {

    Connection cnx = MyConnection.getInstance().getCnx();

    public EventService() {
        ensureParticipantTableExists();
    }

    @Override
    public void add(Event event) {
        if (event.getSlug() == null || event.getSlug().isEmpty()) {
            event.setSlug(generateSlug(event.getName()));
        }
        String req = "INSERT INTO event (name, slug, description, capacity, location, started_at, ended_at, image, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getSlug());
            ps.setString(3, event.getDescription());
            ps.setInt(4, event.getCapacity());
            ps.setString(5, event.getLocation());
            ps.setTimestamp(6, Timestamp.valueOf(event.getStartedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(event.getEndedAt()));
            ps.setString(8, event.getImage());
            ps.setDouble(9, event.getLatitude());
            ps.setDouble(10, event.getLongitude());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    event.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add2(Event event) {
        add(event);
    }

    @Override
    public void delete(Event event) {
        System.out.println("Attempting to delete event ID: " + event.getId());
        String req = "DELETE FROM event WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, event.getId());
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                System.out.println("Successfully deleted ID: " + event.getId());
            } else {
                System.out.println("No event found with ID: " + event.getId() + ". It might be mock data or already deleted.");
                javafx.application.Platform.runLater(() -> {
                    javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
                    alert.setTitle("Delete Failed");
                    alert.setHeaderText("Database ID not found");
                    alert.setContentText("This event cannot be deleted from the database (likely mock data).");
                    alert.show();
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("SQL Error during deletion");
                alert.setContentText(e.getMessage());
                alert.show();
            });
        }
    }

    @Override
    public void update(Event event) {
        String req = "UPDATE event SET name=?, description=?, capacity=?, location=?, started_at=?, ended_at=?, image=?, latitude=?, longitude=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setInt(3, event.getCapacity());
            ps.setString(4, event.getLocation());
            ps.setTimestamp(5, Timestamp.valueOf(event.getStartedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(event.getEndedAt()));
            ps.setString(7, event.getImage());
            ps.setDouble(8, event.getLatitude());
            ps.setDouble(9, event.getLongitude());
            ps.setInt(10, event.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Event> getAll() {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM event ORDER BY started_at DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Event e = new Event();
                e.setId(rs.getInt("id"));
                e.setName(rs.getString("name"));
                e.setSlug(rs.getString("slug"));
                e.setDescription(rs.getString("description"));
                e.setCapacity(rs.getInt("capacity"));
                e.setLocation(rs.getString("location"));
                
                Timestamp start = rs.getTimestamp("started_at");
                if (start != null) e.setStartedAt(start.toLocalDateTime());
                
                Timestamp end = rs.getTimestamp("ended_at");
                if (end != null) e.setEndedAt(end.toLocalDateTime());
                
                e.setImage(rs.getString("image"));
                e.setLatitude(rs.getDouble("latitude"));
                e.setLongitude(rs.getDouble("longitude"));
                
                events.add(e);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Failed to retrieve events");
                alert.setContentText(e.getMessage());
                alert.show();
            });
        }
        return events;
    }

    public boolean joinEvent(int eventId, int userId) {
        ensureParticipantTableExists();
        String insertParticipant = "INSERT IGNORE INTO event_participant (event_id, user_id) VALUES (?, ?)";
        String decrementCapacity = "UPDATE event SET capacity = capacity - 1 WHERE id = ? AND capacity > 0";
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            try (PreparedStatement insertPs = cnx.prepareStatement(insertParticipant);
                 PreparedStatement updatePs = cnx.prepareStatement(decrementCapacity)) {
                insertPs.setInt(1, eventId);
                insertPs.setInt(2, userId);
                int inserted = insertPs.executeUpdate();
                if (inserted <= 0) {
                    cnx.rollback();
                    return false;
                }

                updatePs.setInt(1, eventId);
                int updated = updatePs.executeUpdate();
                if (updated <= 0) {
                    cnx.rollback();
                    return false;
                }

                cnx.commit();

                // Send confirmation email asynchronously to avoid blocking UI
                new Thread(() -> {
                    try {
                        tn.esprit.user.User user = new tn.esprit.services.UserService().getAllUsers().stream()
                                .filter(u -> u.getId() == userId)
                                .findFirst().orElse(null);

                        Event event = getAll().stream().filter(ev -> ev.getId() == eventId).findFirst().orElse(null);

                        if (user != null && event != null) {
                            String body = "<h1>EcoSpot Participation Confirmation</h1>" +
                                          "<p>Hello " + user.getUsername() + ",</p>" +
                                          "<p>You are now registered for: <b>" + event.getName() + "</b></p>" +
                                          "<p><b>Location:</b> " + event.getLocation() + "</p>" +
                                          "<p><b>Date:</b> " + event.getStartedAt().toString() + "</p>" +
                                          "<br><p>Thank you for contributing to a greener planet!</p>";

                            tn.esprit.util.EmailService.sendEmail(user.getEmail(), "Confirmation: Attending " + event.getName(), body);
                        }
                    } catch (Exception e) {
                        System.err.println("Failed to send confirmation email: " + e.getMessage());
                    }
                }).start();

                return true;
            } catch (SQLException inner) {
                cnx.rollback();
                throw inner;
            } finally {
                cnx.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isUserParticipant(int eventId, int userId) {
        ensureParticipantTableExists();
        String req = "SELECT 1 FROM event_participant WHERE event_id = ? AND user_id = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unjoinEvent(int eventId, int userId) {
        ensureParticipantTableExists();
        String deleteParticipant = "DELETE FROM event_participant WHERE event_id = ? AND user_id = ?";
        String incrementCapacity = "UPDATE event SET capacity = capacity + 1 WHERE id = ?";
        boolean previousAutoCommit = true;
        try {
            previousAutoCommit = cnx.getAutoCommit();
            cnx.setAutoCommit(false);

            try (PreparedStatement deletePs = cnx.prepareStatement(deleteParticipant);
                 PreparedStatement updatePs = cnx.prepareStatement(incrementCapacity)) {
                deletePs.setInt(1, eventId);
                deletePs.setInt(2, userId);
                int deleted = deletePs.executeUpdate();
                if (deleted <= 0) {
                    cnx.rollback();
                    return false;
                }

                updatePs.setInt(1, eventId);
                updatePs.executeUpdate();

                cnx.commit();
                return true;
            } catch (SQLException inner) {
                cnx.rollback();
                throw inner;
            } finally {
                cnx.setAutoCommit(previousAutoCommit);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<User> getParticipantsForEvent(int eventId) {
        ensureParticipantTableExists();
        List<User> participants = new ArrayList<>();
        String req = "SELECT u.id, u.username, u.email, u.role " +
                "FROM `user` u JOIN event_participant ep ON ep.user_id = u.id " +
                "WHERE ep.event_id = ? ORDER BY u.username ASC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    participants.add(new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email"),
                            "",
                            rs.getString("role")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return participants;
    }

    private void ensureParticipantTableExists() {
        String req = "CREATE TABLE IF NOT EXISTS event_participant (" +
                "event_id INT NOT NULL, " +
                "user_id INT NOT NULL, " +
                "joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (event_id, user_id), " +
                "INDEX idx_event_participant_event (event_id), " +
                "INDEX idx_event_participant_user (user_id)" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(req);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String generateSlug(String name) {
        if (name == null) return "";
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-");
    }
}
