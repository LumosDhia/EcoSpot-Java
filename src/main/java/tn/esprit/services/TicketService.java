package tn.esprit.services;

import tn.esprit.interfaces.GlobalInterface;
import tn.esprit.ticket.*;
import tn.esprit.util.MyConnection;
import tn.esprit.util.SessionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class TicketService implements GlobalInterface<Ticket> {
    private Connection cnx;
    private final ConsigneService consigneService = new ConsigneService();

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
        boolean hasCreatedById = hasColumn("ticket", "created_by_id");
        boolean hasUpdatedById = hasColumn("ticket", "updated_by_id");
        byte[] currentAppUserId = (hasCreatedById || hasUpdatedById) ? resolveCurrentAppUserIdStrict() : null;
        boolean userIdIsBinary = isBinaryColumn("ticket", "user_id");

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        columns.add("title"); values.add("?");
        columns.add("description"); values.add("?");
        columns.add("location"); values.add("?");
        columns.add("image"); values.add("?");
        columns.add("status"); values.add("?");
        columns.add("priority"); values.add("?");
        columns.add("domain"); values.add("?");
        columns.add("latitude"); values.add("?");
        columns.add("longitude"); values.add("?");
        columns.add("user_id"); values.add("?");
        columns.add("assigned_ngo_id"); values.add("?");
        columns.add("admin_notes"); values.add("?");
        columns.add("completed_by_id"); values.add("?");
        columns.add("completion_message"); values.add("?");
        columns.add("completion_image"); values.add("?");
        columns.add("is_spam"); values.add("?");
        columns.add("created_at"); values.add("?");
        columns.add("achieved_at"); values.add("?");

        if (hasCreatedById) {
            columns.add("created_by_id");
            values.add("?");
        }
        if (hasUpdatedById) {
            columns.add("updated_by_id");
            values.add("?");
        }

        String req = "INSERT INTO `ticket` (" + columns + ") VALUES (" + values + ")";
        try (PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS)) {
            int idx = 1;
            ps.setString(idx++, ticket.getTitle());
            ps.setString(idx++, ticket.getDescription());
            ps.setString(idx++, ticket.getLocation());
            ps.setString(idx++, ticket.getImage());
            ps.setString(idx++, ticket.getStatus().name());
            ps.setString(idx++, ticket.getPriority().name());
            ps.setString(idx++, ticket.getDomain() != null ? ticket.getDomain().name() : null);
            ps.setDouble(idx++, ticket.getLatitude());
            ps.setDouble(idx++, ticket.getLongitude());
            bindUserId(ps, idx++, ticket.getUserId(), userIdIsBinary);
            if (ticket.getAssignedNgoId() != null && ticket.getAssignedNgoId() > 0) ps.setInt(idx++, ticket.getAssignedNgoId());
            else ps.setNull(idx++, Types.INTEGER);
            ps.setString(idx++, ticket.getAdminNotes());
            if (ticket.getCompletedById() != null) ps.setInt(idx++, ticket.getCompletedById());
            else ps.setNull(idx++, Types.INTEGER);
            ps.setString(idx++, ticket.getCompletionMessage());
            ps.setString(idx++, ticket.getCompletionImage());
            ps.setBoolean(idx++, ticket.isSpam());
            ps.setTimestamp(idx++, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));
            if (ticket.getAchievedAt() != null) ps.setTimestamp(idx++, Timestamp.valueOf(ticket.getAchievedAt()));
            else ps.setNull(idx++, Types.TIMESTAMP);
            if (hasCreatedById) {
                if (currentAppUserId != null) ps.setBytes(idx++, currentAppUserId);
                else ps.setNull(idx++, Types.BINARY);
            }
            if (hasUpdatedById) {
                if (currentAppUserId != null) ps.setBytes(idx++, currentAppUserId);
                else ps.setNull(idx++, Types.BINARY);
            }
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int ticketId = generatedKeys.getInt(1);
                    ticket.setId(ticketId);
                    for (tn.esprit.ticket.Consigne c : ticket.getConsignes()) {
                        c.setTicketId(ticketId);
                        consigneService.add(c);
                    }
                }
            }
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

            // Update Consignes: Delete and Re-add
            consigneService.deleteByTicketId(ticket.getId());
            for (Consigne c : ticket.getConsignes()) {
                c.setTicketId(ticket.getId());
                consigneService.add(c);
            }
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
                tickets.add(mapTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getByUserId(int userId) {
        List<Ticket> tickets = new ArrayList<>();
        boolean userIdIsBinary = isBinaryColumn("ticket", "user_id");
        String req = userIdIsBinary
                ? "SELECT * FROM `ticket` WHERE user_id = ? ORDER BY created_at DESC"
                : "SELECT * FROM `ticket` WHERE user_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            if (userIdIsBinary) {
                byte[] current = resolveCurrentAppUserIdStrict();
                if (current == null) {
                    return tickets;
                }
                ps.setBytes(1, current);
            } else {
                ps.setInt(1, userId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tickets.add(mapTicket(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getPendingForAdminReview() {
        List<Ticket> tickets = new ArrayList<>();
        String req = "SELECT * FROM `ticket` WHERE UPPER(status) IN ('PENDING','IN_PROGRESS') ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tickets.add(mapTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public Ticket getById(int id) {
        String req = "SELECT * FROM `ticket` WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapTicket(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Ticket mapTicket(ResultSet rs) throws SQLException {
        Ticket t = new Ticket();
        t.setId(rs.getInt("id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        t.setLocation(rs.getString("location"));
        t.setImage(rs.getString("image"));
        String statusRaw = rs.getString("status");
        try {
            t.setStatus(TicketStatus.valueOf(statusRaw == null ? "PENDING" : statusRaw.trim().toUpperCase()));
        } catch (IllegalArgumentException ex) {
            t.setStatus(TicketStatus.PENDING);
        }
        t.setPriority(TicketPriority.valueOf(rs.getString("priority")));
        String domain = rs.getString("domain");
        if (domain != null) t.setDomain(ActionDomain.valueOf(domain));
        t.setLatitude(rs.getDouble("latitude"));
        t.setLongitude(rs.getDouble("longitude"));
        t.setUserId(0);
        Object uid = rs.getObject("user_id");
        if (uid instanceof Number) t.setUserId(((Number) uid).intValue());

        Object nid = rs.getObject("assigned_ngo_id");
        if (nid instanceof Number) t.setAssignedNgoId(((Number) nid).intValue());

        t.setAdminNotes(rs.getString("admin_notes"));
        Object cid = rs.getObject("completed_by_id");
        if (cid instanceof Number) t.setCompletedById(((Number) cid).intValue());
        t.setCompletionMessage(rs.getString("completion_message"));
        t.setCompletionImage(rs.getString("completion_image"));
        t.setConsignes(consigneService.getByTicketId(t.getId()));
        t.setSpam(rs.getBoolean("is_spam"));
        t.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        Timestamp ach = rs.getTimestamp("achieved_at");
        if (ach != null) t.setAchievedAt(ach.toLocalDateTime());
        return t;
    }

    private boolean hasColumn(String tableName, String columnName) {
        String req = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean isBinaryColumn(String tableName, String columnName) {
        String req = "SELECT DATA_TYPE FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String dataType = rs.getString("DATA_TYPE");
                    return "binary".equalsIgnoreCase(dataType)
                            || "varbinary".equalsIgnoreCase(dataType);
                }
            }
        } catch (SQLException ignored) {
        }
        return false;
    }

    private void bindUserId(PreparedStatement ps, int parameterIndex, int fallbackUserId, boolean binaryUserId) throws SQLException {
        if (!binaryUserId) {
            if (fallbackUserId > 0) {
                ps.setInt(parameterIndex, fallbackUserId);
            } else {
                ps.setNull(parameterIndex, Types.INTEGER);
            }
            return;
        }

        byte[] appUserId = resolveCurrentAppUserIdStrict();
        if (appUserId != null) {
            ps.setBytes(parameterIndex, appUserId);
        } else {
            ps.setNull(parameterIndex, Types.BINARY);
        }
    }

    private byte[] resolveCurrentAppUserIdStrict() {
        if (SessionManager.isLoggedIn() && SessionManager.getCurrentUser() != null) {
            String email = SessionManager.getCurrentUser().getEmail();
            if (email != null && !email.isBlank()) {
                byte[] byEmail = findAppUserIdByEmail(email);
                if (byEmail != null) return byEmail;

                String mapped = mapDemoEmail(email);
                if (mapped != null) {
                    byte[] byMapped = findAppUserIdByEmail(mapped);
                    if (byMapped != null) return byMapped;
                }
            }
        }
        return null;
    }

    private byte[] findAppUserIdByEmail(String email) {
        String req = "SELECT id FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBytes("id");
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private String mapDemoEmail(String email) {
        if (email == null || !email.endsWith("@mail.com")) return null;
        String localPart = email.substring(0, email.indexOf('@'));
        String candidate = localPart + "@ecospot.local";
        String req = "SELECT email FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, candidate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("email");
            }
        } catch (SQLException ignored) {
        }
        return null;
    }
}
