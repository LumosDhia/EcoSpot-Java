package tn.esprit.services;

import tn.esprit.event.Sponsor;
import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorService implements GlobalInterface<Sponsor> {

    Connection cnx = MyConnection.getInstance().getCnx();

    @Override
    public void add(Sponsor sponsor) {
        String req = "INSERT INTO sponsor (name, image, description, sector, location) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, sponsor.getName());
            ps.setString(2, sponsor.getImage());
            ps.setString(3, sponsor.getDescription());
            ps.setString(4, sponsor.getSector());
            ps.setString(5, sponsor.getLocation());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    sponsor.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add2(Sponsor sponsor) {
        add(sponsor);
    }

    @Override
    public void delete(Sponsor sponsor) {
        String req = "DELETE FROM sponsor WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, sponsor.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Sponsor sponsor) {
        String req = "UPDATE sponsor SET name=?, image=?, description=?, sector=?, location=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, sponsor.getName());
            ps.setString(2, sponsor.getImage());
            ps.setString(3, sponsor.getDescription());
            ps.setString(4, sponsor.getSector());
            ps.setString(5, sponsor.getLocation());
            ps.setInt(6, sponsor.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Sponsor> getAll() {
        List<Sponsor> sponsors = new ArrayList<>();
        String req = "SELECT * FROM sponsor ORDER BY name ASC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Sponsor s = new Sponsor();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setImage(rs.getString("image"));
                s.setDescription(rs.getString("description"));
                s.setSector(rs.getString("sector"));
                s.setLocation(rs.getString("location"));
                sponsors.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sponsors;
    }

    // Many-to-Many management
    public void assignSponsorToEvent(int eventId, int sponsorId) {
        String req = "INSERT IGNORE INTO event_sponsor (event_id, sponsor_id) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, eventId);
            ps.setInt(2, sponsorId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void unassignSponsorFromEvent(int eventId, int sponsorId) {
        String req = "DELETE FROM event_sponsor WHERE event_id = ? AND sponsor_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, eventId);
            ps.setInt(2, sponsorId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Sponsor> getSponsorsForEvent(int eventId) {
        List<Sponsor> sponsors = new ArrayList<>();
        String req = "SELECT s.* FROM sponsor s JOIN event_sponsor es ON s.id = es.sponsor_id WHERE es.event_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Sponsor s = new Sponsor();
                s.setId(rs.getInt("id"));
                s.setName(rs.getString("name"));
                s.setImage(rs.getString("image"));
                s.setDescription(rs.getString("description"));
                s.setSector(rs.getString("sector"));
                s.setLocation(rs.getString("location"));
                sponsors.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sponsors;
    }
}
