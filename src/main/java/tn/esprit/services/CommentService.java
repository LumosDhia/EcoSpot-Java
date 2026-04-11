package tn.esprit.services;

import tn.esprit.blog.Comment;
import tn.esprit.user.User;
import tn.esprit.util.SessionManager;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class CommentService {
    private String lastErrorMessage;

    private Connection getCnx() {
        return MyConnection.getInstance().getCnx();
    }

    public CommentService() {
        ensureTableExists();
        ensureModerationColumns();
    }

    private void ensureTableExists() {
        Connection c = getCnx();
        if (c == null) return;
        String req = "CREATE TABLE IF NOT EXISTS `comment` (" +
                     "id INT AUTO_INCREMENT PRIMARY KEY, " +
                     "article_id INT, " +
                     "content TEXT, " +
                     "created_at DATETIME, " +
                     "author_name VARCHAR(255)" +
                     ")";
        try (Statement st = c.createStatement()) {
            st.execute(req);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void ensureModerationColumns() {
        Connection conn = getCnx();
        if (conn == null) return;
        try (Statement st = conn.createStatement()) {
            if (!hasColumn(conn, "comment", "flagged")) {
                st.execute("ALTER TABLE `comment` ADD COLUMN flagged TINYINT(1) NOT NULL DEFAULT 0");
            }
            if (!hasColumn(conn, "comment", "hidden_from_public")) {
                st.execute("ALTER TABLE `comment` ADD COLUMN hidden_from_public TINYINT(1) NOT NULL DEFAULT 0");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean add(Comment c) {
        lastErrorMessage = null;
        Connection conn = getCnx();
        if (conn == null) {
            lastErrorMessage = "Database connection is not available.";
            System.err.println("Cannot add comment: Database connection is null!");
            return false;
        }

        boolean hasAuthorId = hasColumn(conn, "comment", "author_id");
        boolean hasAuthorUserId = hasColumn(conn, "comment", "author_user_id");
        boolean hasAuthor = hasColumn(conn, "comment", "author");
        boolean hasAuthorName = hasColumn(conn, "comment", "author_name");
        boolean hasCreatedAt = hasColumn(conn, "comment", "created_at");
        User currentUser = SessionManager.getCurrentUser();

        StringJoiner columns = new StringJoiner(", ");
        StringJoiner values = new StringJoiner(", ");
        columns.add("article_id");
        values.add("?");
        columns.add("content");
        values.add("?");

        if (hasCreatedAt) {
            columns.add("created_at");
            values.add("NOW()");
        }
        if (hasAuthorName) {
            columns.add("author_name");
            values.add("?");
        }
        if (hasAuthor) {
            columns.add("author");
            values.add("?");
        }
        if (hasAuthorUserId) {
            columns.add("author_user_id");
            values.add("?");
        }
        if (hasAuthorId) {
            columns.add("author_id");
            values.add("?");
        }

        String req = "INSERT INTO `comment` (" + columns + ") VALUES (" + values + ")";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            int idx = 1;
            ps.setInt(idx++, c.getArticleId());
            ps.setString(idx++, c.getContent());
            if (hasAuthorName) {
                ps.setString(idx++, c.getAuthorName() != null ? c.getAuthorName() : "Anonymous User");
            }
            if (hasAuthor) {
                ps.setString(idx++, c.getAuthorName() != null ? c.getAuthorName() : "Anonymous User");
            }
            if (hasAuthorUserId) {
                byte[] appUserId = resolveAppUserIdForComment(conn, c.getArticleId(), currentUser);
                if (appUserId == null) {
                    lastErrorMessage = "No valid app_user UUID found for comment author.";
                    return false;
                }
                ps.setBytes(idx++, appUserId);
            }
            if (hasAuthorId) {
                ps.setNull(idx++, Types.VARCHAR);
            }
            int affected = ps.executeUpdate();
            System.out.println("DEBUG: Comment INSERT successful. ArticleID: " + c.getArticleId() + ", Affected: " + affected);
            return affected > 0;
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            System.err.println("SQL Error adding comment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public List<Comment> getByArticleId(int articleId) {
        List<Comment> comments = new ArrayList<>();
        Connection conn = getCnx();
        if (conn == null) return comments;
        boolean hasAuthorName = hasColumn(conn, "comment", "author_name");
        boolean hasAuthor = hasColumn(conn, "comment", "author");

        boolean hasHiddenFromPublic = hasColumn(conn, "comment", "hidden_from_public");
        boolean hasFlagged = hasColumn(conn, "comment", "flagged");
        String visibilityFilter;
        if (hasHiddenFromPublic) {
            visibilityFilter = " AND COALESCE(hidden_from_public, 0) = 0";
        } else if (hasFlagged) {
            visibilityFilter = " AND COALESCE(flagged, 0) = 0";
        } else {
            visibilityFilter = "";
        }

        String req = "SELECT * FROM `comment` WHERE article_id = ?" + visibilityFilter + " ORDER BY created_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LocalDateTime dt = LocalDateTime.now();
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) dt = ts.toLocalDateTime();

                String authorValue = "Anonymous User";
                if (hasAuthorName) {
                    String fromAuthorName = rs.getString("author_name");
                    if (fromAuthorName != null && !fromAuthorName.isBlank()) {
                        authorValue = fromAuthorName;
                    }
                }
                if ((authorValue == null || authorValue.isBlank() || "Anonymous User".equals(authorValue)) && hasAuthor) {
                    String fromAuthor = rs.getString("author");
                    if (fromAuthor != null && !fromAuthor.isBlank()) {
                        authorValue = fromAuthor;
                    }
                }
                
                comments.add(new Comment(
                    rs.getInt("id"),
                    rs.getInt("article_id"),
                    authorValue,
                    rs.getString("content"),
                    dt
                ));
            }
            System.out.println("DEBUG: Found " + comments.size() + " comments for article " + articleId);
        } catch (SQLException e) {
            System.err.println("SQL Error fetching comments: " + e.getMessage());
            e.printStackTrace();
        }
        return comments;
    }

    public List<Comment> getAllCommentsForAdmin() {
        List<Comment> comments = new ArrayList<>();
        Connection conn = getCnx();
        if (conn == null) return comments;

        boolean hasAuthorName = hasColumn(conn, "comment", "author_name");
        boolean hasAuthor = hasColumn(conn, "comment", "author");
        boolean hasFlagged = hasColumn(conn, "comment", "flagged");

        String authorExpr;
        if (hasAuthorName && hasAuthor) {
            authorExpr = "COALESCE(NULLIF(c.author_name, ''), NULLIF(c.author, ''), 'Anonymous User') AS author_display";
        } else if (hasAuthorName) {
            authorExpr = "COALESCE(NULLIF(c.author_name, ''), 'Anonymous User') AS author_display";
        } else if (hasAuthor) {
            authorExpr = "COALESCE(NULLIF(c.author, ''), 'Anonymous User') AS author_display";
        } else {
            authorExpr = "'Anonymous User' AS author_display";
        }

        String flaggedExpr = hasFlagged ? "c.flagged AS flagged_value" : "0 AS flagged_value";
        String moderationFilter = hasFlagged ? "WHERE COALESCE(c.flagged, 0) = 1 " : "";
        String req = "SELECT c.id, c.article_id, c.content, c.created_at, " + authorExpr + ", " + flaggedExpr +
                     ", a.title AS article_title " +
                     "FROM `comment` c " +
                     "LEFT JOIN article a ON a.id = c.article_id " +
                     moderationFilter +
                     "ORDER BY flagged_value DESC, c.created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDateTime dt = LocalDateTime.now();
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) dt = ts.toLocalDateTime();

                Comment comment = new Comment(
                    rs.getInt("id"),
                    rs.getInt("article_id"),
                    rs.getString("author_display"),
                    rs.getString("content"),
                    dt
                );
                comment.setArticleTitle(rs.getString("article_title"));
                comment.setFlagged(rs.getInt("flagged_value") == 1);
                comments.add(comment);
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
        }
        return comments;
    }

    public void delete(int id) {
        Connection conn = getCnx();
        if (conn == null) return;
        String req = "DELETE FROM `comment` WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateContent(int id, String content) {
        Connection conn = getCnx();
        if (conn == null) return false;
        String req = "UPDATE `comment` SET content = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, content);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public boolean flagComment(int id) {
        Connection conn = getCnx();
        if (conn == null) return false;
        String req = "UPDATE `comment` SET flagged = 1, hidden_from_public = 1 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public boolean acceptFlaggedComment(int id) {
        Connection conn = getCnx();
        if (conn == null) return false;
        String req = "UPDATE `comment` SET flagged = 0, hidden_from_public = 0 WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
            return false;
        }
    }

    public List<Comment> getCommentsAuthoredByCurrentUser() {
        List<Comment> comments = new ArrayList<>();
        Connection conn = getCnx();
        if (conn == null) return comments;

        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return comments;

        boolean hasAuthorName = hasColumn(conn, "comment", "author_name");
        boolean hasAuthor = hasColumn(conn, "comment", "author");
        boolean hasAuthorUserId = hasColumn(conn, "comment", "author_user_id");
        boolean hasFlagged = hasColumn(conn, "comment", "flagged");
        String appUserIdHex = resolveCurrentAppUserIdHex(conn);
        String authorExpr = buildAuthorDisplayExpr(hasAuthorName, hasAuthor);
        String flaggedExpr = hasFlagged ? "COALESCE(c.flagged, 0) AS flagged_value" : "0 AS flagged_value";
        StringBuilder req = new StringBuilder();
        req.append("SELECT c.id, c.article_id, c.content, c.created_at, ")
           .append(authorExpr).append(", ")
           .append("a.title AS article_title, ").append(flaggedExpr).append(" ")
           .append("FROM `comment` c ")
           .append("LEFT JOIN article a ON a.id = c.article_id WHERE ");

        List<String> predicates = new ArrayList<>();
        if (hasAuthorUserId && appUserIdHex != null) {
            predicates.add("HEX(c.author_user_id) = ?");
        }
        if (hasAuthorName) {
            predicates.add("LOWER(c.author_name) = LOWER(?)");
        }
        if (hasAuthor) {
            predicates.add("LOWER(c.author) = LOWER(?)");
        }
        if (predicates.isEmpty()) return comments;

        req.append("(").append(String.join(" OR ", predicates)).append(") ORDER BY c.created_at DESC");

        try (PreparedStatement ps = conn.prepareStatement(req.toString())) {
            int idx = 1;
            if (hasAuthorUserId && appUserIdHex != null) {
                ps.setString(idx++, appUserIdHex);
            }
            if (hasAuthorName) {
                ps.setString(idx++, currentUser.getUsername());
            }
            if (hasAuthor) {
                ps.setString(idx++, currentUser.getUsername());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapCommentRow(rs));
                }
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
        }
        return comments;
    }

    public List<Comment> getCommentsOnCurrentUserArticles() {
        List<Comment> comments = new ArrayList<>();
        Connection conn = getCnx();
        if (conn == null) return comments;

        String ownerEmail = new BlogService().resolveCurrentOwnerEmail();
        if (ownerEmail == null || ownerEmail.isBlank()) return comments;

        boolean hasAuthorName = hasColumn(conn, "comment", "author_name");
        boolean hasAuthor = hasColumn(conn, "comment", "author");
        boolean hasFlagged = hasColumn(conn, "comment", "flagged");
        String authorExpr = buildAuthorDisplayExpr(hasAuthorName, hasAuthor);
        String flaggedExpr = hasFlagged ? "COALESCE(c.flagged, 0) AS flagged_value" : "0 AS flagged_value";

        String req = "SELECT c.id, c.article_id, c.content, c.created_at, " +
                     authorExpr + ", " +
                     "a.title AS article_title, " + flaggedExpr + " " +
                     "FROM `comment` c " +
                     "JOIN article a ON a.id = c.article_id " +
                     "JOIN app_user au ON au.id = a.created_by_id " +
                     "WHERE LOWER(au.email) = LOWER(?) " +
                     "ORDER BY c.created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, ownerEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comments.add(mapCommentRow(rs));
                }
            }
        } catch (SQLException e) {
            lastErrorMessage = e.getMessage();
            e.printStackTrace();
        }
        return comments;
    }

    private Comment mapCommentRow(ResultSet rs) throws SQLException {
        LocalDateTime dt = LocalDateTime.now();
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) dt = ts.toLocalDateTime();

        Comment comment = new Comment(
            rs.getInt("id"),
            rs.getInt("article_id"),
            rs.getString("author_display"),
            rs.getString("content"),
            dt
        );
        comment.setArticleTitle(rs.getString("article_title"));
        comment.setFlagged(rs.getInt("flagged_value") == 1);
        return comment;
    }

    private String buildAuthorDisplayExpr(boolean hasAuthorName, boolean hasAuthor) {
        if (hasAuthorName && hasAuthor) {
            return "COALESCE(NULLIF(c.author_name, ''), NULLIF(c.author, ''), 'Anonymous User') AS author_display";
        } else if (hasAuthorName) {
            return "COALESCE(NULLIF(c.author_name, ''), 'Anonymous User') AS author_display";
        } else if (hasAuthor) {
            return "COALESCE(NULLIF(c.author, ''), 'Anonymous User') AS author_display";
        }
        return "'Anonymous User' AS author_display";
    }

    private String resolveCurrentAppUserIdHex(Connection conn) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null || currentUser.getEmail() == null || currentUser.getEmail().isBlank()) {
            return null;
        }

        String byEmail = findAppUserIdHexByEmail(conn, currentUser.getEmail());
        if (byEmail != null) {
            return byEmail;
        }

        String mappedEmail = mapDemoEmailToAppUserEmail(conn, currentUser.getEmail());
        if (mappedEmail != null) {
            return findAppUserIdHexByEmail(conn, mappedEmail);
        }
        return null;
    }

    private String findAppUserIdHexByEmail(Connection conn, String email) {
        String req = "SELECT HEX(id) AS id_hex FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("id_hex");
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private String mapDemoEmailToAppUserEmail(Connection conn, String email) {
        if (email == null || !email.endsWith("@mail.com")) {
            return null;
        }
        String localPart = email.substring(0, email.indexOf('@'));
        String candidate = localPart + "@ecospot.local";
        String req = "SELECT email FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, candidate);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("email");
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) {
        String req = "SELECT 1 FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    private byte[] resolveAppUserIdForComment(Connection conn, int articleId, User currentUser) {
        if (currentUser != null && currentUser.getEmail() != null && !currentUser.getEmail().isBlank()) {
            byte[] byEmail = findAppUserIdByEmail(conn, currentUser.getEmail());
            if (byEmail != null) {
                return byEmail;
            }
        }
        return findArticleCreatorAppUserId(conn, articleId);
    }

    private byte[] findAppUserIdByEmail(Connection conn, String email) {
        String req = "SELECT id FROM app_user WHERE email = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("id");
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }

    private byte[] findArticleCreatorAppUserId(Connection conn, int articleId) {
        String req = "SELECT created_by_id FROM article WHERE id = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, articleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("created_by_id");
                }
            }
        } catch (SQLException ignored) {
        }
        return null;
    }
}
