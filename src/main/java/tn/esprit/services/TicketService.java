package tn.esprit.services;

import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.ticket.*;
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
                "`title` VARCHAR(255)," +
                "`description` TEXT," +
                "`location` VARCHAR(255)," +
                "`image` VARCHAR(255)," +
                "`status` VARCHAR(50)," +
                "`priority` VARCHAR(50)," +
                "`domain` VARCHAR(50)," +
                "`latitude` DOUBLE," +
                "`longitude` DOUBLE," +
                "`user_id` INT," +
                "`assigned_ngo_id` INT," +
                "`admin_notes` TEXT," +
                "`completed_by_id` INT," +
                "`completion_message` TEXT," +
                "`completion_image` VARCHAR(255)," +
                "`is_spam` TINYINT(1) DEFAULT 0," +
                "`created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "`achieved_at` TIMESTAMP NULL" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(req);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void add(Ticket ticket) {
        String req = "INSERT INTO `ticket` (title, description, location, image, status, priority, domain, latitude, longitude, user_id, assigned_ngo_id, is_spam, created_at, updated_at, created_by_id, updated_by_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getLocation());
            ps.setString(4, ticket.getImage());
            ps.setString(5, ticket.getStatus().name());
            ps.setString(6, ticket.getPriority().name());
            ps.setString(7, ticket.getDomain() != null ? ticket.getDomain().name() : null);
            ps.setDouble(8, ticket.getLatitude());
            ps.setDouble(9, ticket.getLongitude());
            // Defensive handling for binary user_id / assigned_ngo_id
            if (ticket.getUserId() > 0) ps.setInt(10, ticket.getUserId());
            else ps.setNull(10, Types.BINARY);
            
            if (ticket.getAssignedNgoId() != null && ticket.getAssignedNgoId() > 0) ps.setInt(11, ticket.getAssignedNgoId());
            else ps.setNull(11, Types.BINARY);
            
            ps.setBoolean(12, ticket.isSpam());
            ps.setTimestamp(13, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            ps.setTimestamp(14, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            
            byte[] dummyUuid = new byte[16]; // Default empty binary(16)
            ps.setBytes(15, dummyUuid); // created_by_id
            ps.setBytes(16, dummyUuid); // updated_by_id
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("SQL Error adding ticket: " + e.getMessage(), e);
        }
    }

    @Override
    public void add2(Ticket ticket) {}

    @Override
    public void delete(Ticket ticket) {
        String req = "DELETE FROM `ticket` WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, ticket.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void update(Ticket ticket) {
        String req = "UPDATE `ticket` SET title=?, description=?, location=?, image=?, status=?, priority=?, domain=?, latitude=?, longitude=?, assigned_ngo_id=?, admin_notes=?, completed_by_id=?, completion_message=?, completion_image=?, is_spam=?, achieved_at=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, ticket.getTitle());
            ps.setString(2, ticket.getDescription());
            ps.setString(3, ticket.getLocation());
            ps.setString(4, ticket.getImage());
            ps.setString(5, ticket.getStatus().name());
            ps.setString(6, ticket.getPriority().name());
            ps.setString(7, ticket.getDomain() != null ? ticket.getDomain().name() : null);
            ps.setDouble(8, ticket.getLatitude());
            ps.setDouble(9, ticket.getLongitude());
            
            if (ticket.getAssignedNgoId() != null) ps.setInt(10, ticket.getAssignedNgoId());
            else ps.setNull(10, Types.INTEGER);
            
            ps.setString(11, ticket.getAdminNotes());
            
            if (ticket.getCompletedById() != null) ps.setInt(12, ticket.getCompletedById());
            else ps.setNull(12, Types.INTEGER);
            
            ps.setString(13, ticket.getCompletionMessage());
            ps.setString(14, ticket.getCompletionImage());
            ps.setBoolean(15, ticket.isSpam());
            
            if (ticket.getAchievedAt() != null) ps.setTimestamp(16, Timestamp.valueOf(ticket.getAchievedAt()));
            else ps.setNull(16, Types.TIMESTAMP);
            
            ps.setInt(17, ticket.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<Ticket> getAll() {
        List<Ticket> tickets = new ArrayList<>();
        String req = "SELECT * FROM `ticket` ORDER BY created_at DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                Ticket t = new Ticket();
                t.setId(rs.getInt("id"));
                t.setTitle(rs.getString("title"));
                t.setDescription(rs.getString("description"));
                t.setLocation(rs.getString("location"));
                t.setImage(rs.getString("image"));
                t.setStatus(TicketStatus.valueOf(rs.getString("status")));
                t.setPriority(TicketPriority.valueOf(rs.getString("priority")));
                String domain = rs.getString("domain");
                if (domain != null) t.setDomain(ActionDomain.valueOf(domain));
                t.setLatitude(rs.getDouble("latitude"));
                t.setLongitude(rs.getDouble("longitude"));
                t.setUserId(0); // Default if conversion fails
                try {
                    Object uid = rs.getObject("user_id");
                    if (uid instanceof Number) t.setUserId(((Number) uid).intValue());
                } catch (Exception e) { /* Ignore binary IDs for now */ }
                
                int ngoId = 0;
                try {
                    Object nid = rs.getObject("assigned_ngo_id");
                    if (nid instanceof Number) {
                        t.setAssignedNgoId(((Number) nid).intValue());
                    }
                } catch (Exception e) { /* Ignore */ }
                
                t.setAdminNotes(rs.getString("admin_notes"));
                
                try {
                    Object cid = rs.getObject("completed_by_id");
                    if (cid instanceof Number) {
                        t.setCompletedById(((Number) cid).intValue());
                    }
                } catch (Exception e) { /* Ignore */ }
                
                t.setCompletionMessage(rs.getString("completion_message"));
                t.setCompletionImage(rs.getString("completion_image"));
                t.setSpam(rs.getBoolean("is_spam"));
                t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                
                Timestamp ach = rs.getTimestamp("achieved_at");
                if (ach != null) t.setAchievedAt(ach.toLocalDateTime());
                
                tickets.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }
}
