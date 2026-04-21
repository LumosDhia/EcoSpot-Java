package tn.esprit.services;

import tn.esprit.util.MyConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsService {

    private final Connection cnx = MyConnection.getInstance().getCnx();

    public int getTotalViews(LocalDate from, LocalDate to) {
        String sql = "SELECT COUNT(*) FROM article_view_event WHERE DATE(viewed_at) BETWEEN ? AND ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Map<String, Object>> getViewsTimeSeries(LocalDate from, LocalDate to) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT DATE(viewed_at) AS day, COUNT(*) AS views " +
                     "FROM article_view_event " +
                     "WHERE DATE(viewed_at) BETWEEN ? AND ? " +
                     "GROUP BY day ORDER BY day";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", rs.getString("day"));
                map.put("views", rs.getInt("views"));
                result.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getTopArticlesByViews(LocalDate from, LocalDate to, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT a.id, a.title, COUNT(ave.id) AS view_count " +
                     "FROM article a " +
                     "LEFT JOIN article_view_event ave ON ave.article_id = a.id AND DATE(ave.viewed_at) BETWEEN ? AND ? " +
                     "GROUP BY a.id, a.title " +
                     "ORDER BY view_count DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("title", rs.getString("title"));
                map.put("views", rs.getInt("view_count"));
                result.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Phase 3.1.1 Overview Stats
    public int getTotalPublishedArticles() {
        String sql = "SELECT COUNT(*) FROM article";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTotalComments() {
        String sql = "SELECT COUNT(*) FROM comment";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Integer> getTotalReactions() {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT type, COUNT(*) as cnt FROM article_reaction_java GROUP BY type";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                String r = rs.getString("type");
                if (r != null) {
                    result.put(r.toUpperCase(), rs.getInt("cnt"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 3.1.2 Time-Series
    public List<Map<String, Object>> getViewsWeekly(int weeksBack) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT YEARWEEK(viewed_at) as week, COUNT(*) as views FROM article_view_event WHERE viewed_at >= DATE_SUB(CURDATE(), INTERVAL ? WEEK) GROUP BY week ORDER BY week";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, weeksBack);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("week", rs.getString("week"));
                map.put("views", rs.getInt("views"));
                result.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<Map<String, Object>> getViewsByHourOfDay() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT HOUR(viewed_at) AS hour_of_day, COUNT(*) AS views FROM article_view_event GROUP BY hour_of_day ORDER BY hour_of_day";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("hour", rs.getInt("hour_of_day"));
                map.put("views", rs.getInt("views"));
                result.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Phase 3.1.5 Category & Tag Stats
    public List<Map<String, Object>> getCategoryStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT c.name, COUNT(DISTINCT a.id) AS article_count, COUNT(ave.id) AS total_views " +
                     "FROM category c " +
                     "LEFT JOIN article a ON a.category_id = c.id " +
                     "LEFT JOIN article_view_event ave ON ave.article_id = a.id " +
                     "GROUP BY c.id, c.name " +
                     "ORDER BY total_views DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", rs.getString("name"));
                map.put("articles", rs.getInt("article_count"));
                map.put("views", rs.getInt("total_views"));
                result.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Phase 3.1.7 Search Terms
    public List<Map<String, Object>> getTopSearchTerms(LocalDate from, LocalDate to, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT term, COUNT(*) AS search_count, AVG(result_count) AS avg_results " +
                     "FROM search_term_log " +
                     "WHERE DATE(searched_at) BETWEEN ? AND ? " +
                     "GROUP BY term " +
                     "ORDER BY search_count DESC " +
                     "LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            ps.setInt(3, limit);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("term", rs.getString("term"));
                map.put("count", rs.getInt("search_count"));
                map.put("avg_results", rs.getDouble("avg_results"));
                result.add(map);
            }
        } catch(SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Phase 3.1.3 Top Content (Remaining)
    public List<Map<String, Object>> getTopArticlesByEngagement(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT a.id, a.title, " +
                     "(COUNT(DISTINCT ave.id) + COUNT(DISTINCT c.id) + COUNT(DISTINCT are.id)) as engagement " +
                     "FROM article a " +
                     "LEFT JOIN article_view_event ave ON ave.article_id = a.id " +
                     "LEFT JOIN comment c ON c.article_id = a.id " +
                     "LEFT JOIN article_reaction_event are ON are.article_id = a.id " +
                     "GROUP BY a.id, a.title ORDER BY engagement DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("title", rs.getString("title"));
                map.put("engagement", rs.getInt("engagement"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 3.1.4 Author Stats
    public List<Map<String, Object>> getAuthorStats(LocalDate from, LocalDate to) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT u.username, COUNT(DISTINCT a.id) as article_count, " +
                     "COALESCE(SUM(asd.views), 0) as total_views " +
                     "FROM user u " +
                     "JOIN article a ON a.writer_id = u.id " +
                     "LEFT JOIN article_stats_daily asd ON asd.article_id = a.id " +
                     "AND asd.stat_date BETWEEN ? AND ? " +
                     "GROUP BY u.id, u.username ORDER BY total_views DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("author", rs.getString("username"));
                map.put("articles", rs.getInt("article_count"));
                map.put("views", rs.getInt("total_views"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 3.1.5 Category & Tag Stats (Tag part)
    public List<Map<String, Object>> getTagStats(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        // Assuming a many-to-many relationship table article_tags exists
        String sql = "SELECT t.name, COUNT(at.article_id) as article_count " +
                     "FROM tag t JOIN article_tags at ON t.id = at.tag_id " +
                     "GROUP BY t.id, t.name ORDER BY article_count DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("tag", rs.getString("name"));
                map.put("count", rs.getInt("article_count"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 3.1.6 Comment Analytics
    public List<Map<String, Object>> getTopCommenters(int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT u.username, COUNT(c.id) as comment_count " +
                     "FROM user u JOIN comment c ON u.id = c.user_id " +
                     "GROUP BY u.id, u.username ORDER BY comment_count DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("username", rs.getString("username"));
                map.put("count", rs.getInt("comment_count"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 3.1.8 Publishing Patterns
    public Map<String, Integer> getArticleStatusFunnel() {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as cnt FROM article GROUP BY status";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 4.5 Per-Article Drilldown
    public Map<String, Object> getArticleBasicStats(int articleId) {
        Map<String, Object> result = new HashMap<>();
        String sql = "SELECT a.title, COALESCE(u.firstname, 'Unknown') as author, a.published_at, a.views, " +
                     "(SELECT COUNT(*) FROM article_reaction_java WHERE article_id = a.id AND type = 'like') as likes, " +
                     "(SELECT COUNT(*) FROM article_reaction_java WHERE article_id = a.id AND type = 'dislike') as dislikes, " +
                     "(SELECT COUNT(*) FROM `comment` WHERE article_id = a.id) as comments " +
                     "FROM article a LEFT JOIN app_user u ON a.writer_id = u.id WHERE a.id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                result.put("title", rs.getString("title"));
                result.put("author", rs.getString("author"));
                result.put("published_at", rs.getTimestamp("published_at"));
                result.put("views", rs.getInt("views"));
                result.put("likes", rs.getInt("likes"));
                result.put("dislikes", rs.getInt("dislikes"));
                result.put("comments", rs.getInt("comments"));
            } else {
                // Article not found — still return something so the UI doesn't blank out
                result.put("title", "Article #" + articleId);
                result.put("author", "Unknown");
                result.put("published_at", null);
                result.put("views", 0);
                result.put("likes", 0);
                result.put("dislikes", 0);
                result.put("comments", 0);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Map<String, Object>> getArticleViewsTimeline(int articleId, int daysBack) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT DATE(viewed_at) as day, COUNT(*) as views " +
                     "FROM article_view_event WHERE article_id = ? " +
                     "AND viewed_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "GROUP BY day ORDER BY day";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ps.setInt(2, daysBack);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", rs.getString("day"));
                map.put("views", rs.getInt("views"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }
}
