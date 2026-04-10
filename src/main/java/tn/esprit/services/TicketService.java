package tn.esprit.services;

import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.ticket.Ticket;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketService implements GlobalInterface<Ticket> {
    private Connection cnx;

    public TicketService() {
        cnx = MyConnection.getInstance().getCnx();
        ensureTableExists();
    }

    private void ensureTableExists() {
        String req = "CREATE TABLE IF NOT EXISTS `ticket` (" +
                "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                "`event_id` INT," +
                "`price` DOUBLE," +
                "`type` VARCHAR(50)" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(req);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Ticket ticket) {
        String req = "INSERT INTO `ticket` (event_id, price, type) VALUES (?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, ticket.getEventId());
            ps.setDouble(2, ticket.getPrice());
            ps.setString(3, ticket.getType());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add2(Ticket ticket) {}

    @Override
    public void delete(Ticket ticket) {}

    @Override
    public void update(Ticket ticket) {}

    @Override
    public List<Ticket> getAll() {
        return new ArrayList<>();
    }
}
