package tn.esprit.services;

import tn.esprit.blog.Comment;
import tn.esprit.user.User;
import tn.esprit.util.SessionManager;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentService {
    private String lastErrorMessage;

    private static final String[] GUEST_ADJECTIVES = {
        "Green", "Eco", "Solar", "Windy", "Pure", "Wild", "Earth", "Nature", "Ocean", "Forest",
        "Happy", "Caring", "Wise", "Brave", "Swift", "Quiet", "Bright", "Steady", "Loyal", "Kind"
    };

    private static final String[] GUEST_NOUNS = {
        "Traveler", "Guardian", "Explorer", "Lover", "Protector", "Friend", "Warrior", "Spirit", "Breeze", "Seed",
        "Panda", "Turtle", "Oak", "Leaf", "River", "Sprout", "Bloom", "Shadow", "Light", "Mountain"
    };

    private String getGuestName(int id) {
        if (id <= 0) return "Guest " + ((int) (Math.random() * 9000) + 1000);
        int adjIndex = (id * 31) % GUEST_ADJECTIVES.length;
        int nounIndex = (id * 17) % GUEST_NOUNS.length;
        if (adjIndex < 0) adjIndex += GUEST_ADJECTIVES.length;
        if (nounIndex < 0) nounIndex += GUEST_NOUNS.length;
        return GUEST_ADJECTIVES[adjIndex] + " " + GUEST_NOUNS[nounIndex];
    }

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
                     "created_at DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                     "author_name VARCHAR(255), " +
                     "flagged TINYINT(1) NOT NULL DEFAULT 0, " +
                     "hidden_from_public TINYINT(1) NOT NULL DEFAULT 0" +
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

    private String filterProfanity(String text) {
        if (text == null || text.isBlank()) return text;
        try {
            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            String encodedText = java.net.URLEncoder.encode(text, java.nio.charset.StandardCharsets.UTF_8.toString());
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create("https://www.purgomalum.com/service/plain?text=" + encodedText))
                    .GET()
                    .build();
            java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return response.body();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    public boolean add(Comment c) {
        lastErrorMessage = null;
        Connection conn = getCnx();
        if (conn == null) {
            lastErrorMessage = "Database connection is not available.";
            return false;
        }

        // Apply PurgoMalum Profanity Filter
        c.setContent(filterProfanity(c.getContent()));

        User currentUser = SessionManager.getCurrentUser();
        String authorName = (currentUser != null && currentUser.getUsername() != null)
                ? currentUser.getUsername()
                : (c.getAuthorName() != null && !c.getAuthorName().equalsIgnoreCase("Anonymous User") ? c.getAuthorName() : getGuestName(0));

        String req = "INSERT INTO `comment` (article_id, content, created_at, author_name) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, c.getArticleId());
            ps.setString(2, c.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, authorName);
            int affected = ps.executeUpdate();
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

        boolean hasHiddenFromPublic = hasColumn(conn, "comment", "hidden_from_public");
        boolean hasFlagged = hasColumn(conn, "comment", "flagged");
        String visibilityFilter;
        if (hasHiddenFromPublic) {
            visibilityFilter = " AND COALESCE(c.hidden_from_public, 0) = 0";
        } else if (hasFlagged) {
            visibilityFilter = " AND COALESCE(c.flagged, 0) = 0";
        } else {
            visibilityFilter = "";
        }

        String req = "SELECT c.*, u.avatar_style FROM `comment` c " +
                     "LEFT JOIN `user` u ON c.author_name = u.username " +
                     "WHERE c.article_id = ?" + visibilityFilter + " ORDER BY c.created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LocalDateTime dt = LocalDateTime.now();
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) dt = ts.toLocalDateTime();

                String authorValue = rs.getString("author_name");
                int commentId = rs.getInt("id");
                if (authorValue == null || authorValue.isBlank() || authorValue.equalsIgnoreCase("Anonymous User")) {
                    authorValue = getGuestName(commentId);
                }

                Comment comment = new Comment(
                    rs.getInt("id"),
                    rs.getInt("article_id"),
                    authorValue,
                    rs.getString("content"),
                    dt
                );

                try { comment.setAvatarStyle(rs.getString("avatar_style")); } catch (Exception ignored) {}
                comments.add(comment);
            }
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

        boolean hasFlagged = hasColumn(conn, "comment", "flagged");
        String flaggedExpr = hasFlagged ? "c.flagged AS flagged_value" : "0 AS flagged_value";

        String req = "SELECT c.id, c.article_id, c.content, c.created_at, " +
                     "c.author_name AS author_raw, " +
                     flaggedExpr + ", a.title AS article_title " +
                     "FROM `comment` c " +
                     "LEFT JOIN article a ON a.id = c.article_id " +
                     "ORDER BY flagged_value DESC, c.created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                LocalDateTime dt = LocalDateTime.now();
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null) dt = ts.toLocalDateTime();

                String authorDisplay = rs.getString("author_raw");
                int commentId = rs.getInt("id");
                if (authorDisplay == null || authorDisplay.isBlank() || authorDisplay.equalsIgnoreCase("Anonymous User")) {
                    authorDisplay = getGuestName(commentId);
                }

                Comment comment = new Comment(
                    commentId,
                    rs.getInt("article_id"),
                    authorDisplay,
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
        
        // Apply PurgoMalum Profanity Filter
        content = filterProfanity(content);
        
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

        boolean hasFlagged = hasColumn(conn, "comment", "flagged");
        String flaggedExpr = hasFlagged ? "COALESCE(c.flagged, 0) AS flagged_value" : "0 AS flagged_value";

        String req = "SELECT c.id, c.article_id, c.content, c.created_at, " +
                     "c.author_name AS author_raw, " +
                     "a.title AS article_title, " + flaggedExpr + " " +
                     "FROM `comment` c LEFT JOIN article a ON a.id = c.article_id " +
                     "WHERE LOWER(c.author_name) = LOWER(?) ORDER BY c.created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, currentUser.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) comments.add(mapCommentRow(rs));
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

        boolean hasFlagged = hasColumn(conn, "comment", "flagged");
        String flaggedExpr = hasFlagged ? "COALESCE(c.flagged, 0) AS flagged_value" : "0 AS flagged_value";

        String req = "SELECT c.id, c.article_id, c.content, c.created_at, " +
                     "c.author_name AS author_raw, " +
                     "a.title AS article_title, " + flaggedExpr + " " +
                     "FROM `comment` c " +
                     "JOIN article a ON a.id = c.article_id " +
                     "JOIN user au ON au.id = a.created_by_id " +
                     "WHERE LOWER(au.email) = LOWER(?) " +
                     "ORDER BY c.created_at DESC";

        try (PreparedStatement ps = conn.prepareStatement(req)) {
            ps.setString(1, ownerEmail);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) comments.add(mapCommentRow(rs));
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

        String authorDisplay = rs.getString("author_raw");
        int commentId = rs.getInt("id");
        if (authorDisplay == null || authorDisplay.isBlank() || authorDisplay.equalsIgnoreCase("Anonymous User")) {
            authorDisplay = getGuestName(commentId);
        }

        Comment comment = new Comment(
            commentId,
            rs.getInt("article_id"),
            authorDisplay,
            rs.getString("content"),
            dt
        );
        comment.setArticleTitle(rs.getString("article_title"));
        comment.setFlagged(rs.getInt("flagged_value") == 1);
        return comment;
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
}
