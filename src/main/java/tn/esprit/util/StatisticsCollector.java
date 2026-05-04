package tn.esprit.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatisticsCollector {

    private static StatisticsCollector instance;
    private StatisticsCollector() {
    }

    private Connection getCnx() {
        return MyConnection.getInstance().getCnx();
    }

    public static StatisticsCollector getInstance() {
        if (instance == null) {
            instance = new StatisticsCollector();
        }
        return instance;
    }

    public void recordView(int articleId, String sessionId, Integer userId) {
        String req = "INSERT INTO article_view_event (article_id, session_id, user_id, viewed_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, articleId);
            ps.setString(2, sessionId);
            if (userId != null) {
                ps.setInt(3, userId);
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }
            ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordReaction(int articleId, Integer userId, String reactionType) {
        String req = "INSERT INTO article_reaction_event (article_id, user_id, reaction, acted_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setInt(1, articleId);
            if (userId != null) {
                ps.setInt(2, userId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.setString(3, reactionType);
            ps.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void recordSearchTerm(String term, int resultCount) {
        String req = "INSERT INTO search_term_log (term, result_count, searched_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.setString(1, term);
            ps.setInt(2, resultCount);
            ps.setTimestamp(3, new java.sql.Timestamp(System.currentTimeMillis()));
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void flushDailySnapshot() {
        String req = "INSERT INTO article_stats_daily (article_id, stat_date, views, likes, dislikes, comments) " +
                "SELECT " +
                "    ave.article_id, " +
                "    DATE(ave.viewed_at) AS stat_date, " +
                "    COUNT(DISTINCT ave.id) AS views, " +
                "    (SELECT COUNT(*) FROM article_reaction_event WHERE article_id = ave.article_id AND reaction = 'LIKE' AND DATE(acted_at) = DATE(ave.viewed_at)) AS likes, " +
                "    (SELECT COUNT(*) FROM article_reaction_event WHERE article_id = ave.article_id AND reaction = 'DISLIKE' AND DATE(acted_at) = DATE(ave.viewed_at)) AS dislikes, " +
                "    (SELECT COUNT(*) FROM comment WHERE article_id = ave.article_id AND DATE(created_at) = DATE(ave.viewed_at)) AS comments " +
                "FROM article_view_event ave " +
                "WHERE DATE(ave.viewed_at) = CURDATE() - INTERVAL 1 DAY " +
                "GROUP BY ave.article_id, stat_date " +
                "ON DUPLICATE KEY UPDATE " +
                "views = VALUES(views), likes = VALUES(likes), dislikes = VALUES(dislikes), comments = VALUES(comments)";
        try (PreparedStatement ps = getCnx().prepareStatement(req)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
