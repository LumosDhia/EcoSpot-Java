package tn.esprit.services;

import tn.esprit.ticket.Consigne;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsigneService {
    private Connection cnx;

    public ConsigneService() {
        cnx = MyConnection.getInstance().getCnx();
        ensureTableExists();
    }

    private void ensureTableExists() {
        String req = "CREATE TABLE IF NOT EXISTS `consigne` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`text` TEXT NOT NULL," +
                "`ticket_id` INT," +
                "CONSTRAINT `fk_consigne_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `ticket` (`id`) ON DELETE CASCADE" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(req);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void add(Consigne c) {
        String req = "INSERT INTO `consigne` (text, ticket_id) VALUES (?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, c.getText());
            ps.setInt(2, c.getTicketId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Consigne> getByTicketId(int ticketId) {
        List<Consigne> list = new ArrayList<>();
        String req = "SELECT * FROM `consigne` WHERE ticket_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, ticketId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Consigne(rs.getInt("id"), rs.getString("text"), rs.getInt("ticket_id")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void deleteByTicketId(int ticketId) {
        String req = "DELETE FROM `consigne` WHERE ticket_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, ticketId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
