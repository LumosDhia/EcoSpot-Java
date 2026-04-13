package tn.esprit.services;

import tn.esprit.event.Event;
import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements GlobalInterface<Event> {

    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void add(Event event) {
        String req = "INSERT INTO event (name, slug, description, capacity, location, started_at, ended_at, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getSlug());
            ps.setString(3, event.getDescription());
            ps.setInt(4, event.getCapacity());
            ps.setString(5, event.getLocation());
            ps.setTimestamp(6, Timestamp.valueOf(event.getStartedAt()));
            ps.setTimestamp(7, Timestamp.valueOf(event.getEndedAt()));
            ps.setString(8, event.getImage());
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
        String req = "UPDATE event SET name=?, description=?, capacity=?, location=?, started_at=?, ended_at=?, image=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, event.getName());
            ps.setString(2, event.getDescription());
            ps.setInt(3, event.getCapacity());
            ps.setString(4, event.getLocation());
            ps.setTimestamp(5, Timestamp.valueOf(event.getStartedAt()));
            ps.setTimestamp(6, Timestamp.valueOf(event.getEndedAt()));
            ps.setString(7, event.getImage());
            ps.setInt(8, event.getId());
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
}
