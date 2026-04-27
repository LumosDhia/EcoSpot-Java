package tn.esprit.services;

import tn.esprit.util.MyConnection;

import java.sql.*;

public class ReactionService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    public ReactionService() {
        ensureTable();
    }

    private void ensureTable() {
        String sql = "CREATE TABLE IF NOT EXISTS article_reaction (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "article_id INT NOT NULL," +
                "user_id INT NOT NULL," +
                "type ENUM('like','dislike') NOT NULL," +
                "UNIQUE KEY uniq_article_user (article_id, user_id)" +
                ")";
        try (Statement st = cnx.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** Returns current user's reaction type ("like", "dislike") or null if none. */
    public String getUserReaction(int articleId, int userId) {
        String sql = "SELECT type FROM article_reaction WHERE article_id = ? AND user_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("type");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Toggle a reaction. If the user already has the same type → remove it.
     * If different type → switch it. If none → insert it.
     * Returns the new reaction type, or null if it was removed.
     */
    public String toggleReaction(int articleId, int userId, String type) {
        String existing = getUserReaction(articleId, userId);
        try {
            if (existing == null) {
                // Insert
                String sql = "INSERT INTO article_reaction (article_id, user_id, type) VALUES (?, ?, ?)";
                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    ps.setInt(1, articleId);
                    ps.setInt(2, userId);
                    ps.setString(3, type);
                    ps.executeUpdate();
                    tn.esprit.util.StatisticsCollector.getInstance().recordReaction(articleId, userId, "like".equalsIgnoreCase(type) ? "LIKE" : "DISLIKE");
                }
                return type;
            } else if (existing.equals(type)) {
                // Remove (toggle off)
                String sql = "DELETE FROM article_reaction WHERE article_id = ? AND user_id = ?";
                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    ps.setInt(1, articleId);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                return null;
            } else {
                // Switch
                String sql = "UPDATE article_reaction SET type = ? WHERE article_id = ? AND user_id = ?";
                try (PreparedStatement ps = cnx.prepareStatement(sql)) {
                    ps.setString(1, type);
                    ps.setInt(2, articleId);
                    ps.setInt(3, userId);
                    ps.executeUpdate();
                    tn.esprit.util.StatisticsCollector.getInstance().recordReaction(articleId, userId, "like".equalsIgnoreCase(type) ? "LIKE" : "DISLIKE");
                }
                return type;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return existing;
    }

    public int getLikes(int articleId) {
        return countReactions(articleId, "like");
    }

    public int getDislikes(int articleId) {
        return countReactions(articleId, "dislike");
    }

    private int countReactions(int articleId, String type) {
        String sql = "SELECT COUNT(*) FROM article_reaction WHERE article_id = ? AND type = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.setString(2, type);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println(">>> LIVE DB COUNT Article " + articleId + " (" + type + "): " + count);
                return count;
            }
        } catch (SQLException e) {
            System.err.println("SQL Error counting reactions: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }
}
