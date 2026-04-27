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

    // ─── owner-scoped helpers ─────────────────────────────────────────────────
    // ownerEmail=null → no filter (admin sees all); non-null → NGO sees own articles only

    private static final String NGO_ARTICLE_IDS =
        "SELECT a.id FROM article a " +
        "JOIN user u ON u.id = a.created_by_id " +
        "WHERE LOWER(u.email) = LOWER(?)";

    public int getTotalViews(LocalDate from, LocalDate to, String ownerEmail) {
        String sql = ownerEmail == null
            ? "SELECT COUNT(*) FROM article_view_event WHERE DATE(viewed_at) BETWEEN ? AND ?"
            : "SELECT COUNT(*) FROM article_view_event ave " +
              "WHERE DATE(ave.viewed_at) BETWEEN ? AND ? " +
              "AND ave.article_id IN (" + NGO_ARTICLE_IDS + ")";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            if (ownerEmail != null) ps.setString(3, ownerEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<Map<String, Object>> getViewsTimeSeries(LocalDate from, LocalDate to, String ownerEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = ownerEmail == null
            ? "SELECT DATE(viewed_at) AS day, COUNT(*) AS views FROM article_view_event WHERE DATE(viewed_at) BETWEEN ? AND ? GROUP BY day ORDER BY day"
            : "SELECT DATE(ave.viewed_at) AS day, COUNT(*) AS views FROM article_view_event ave " +
              "WHERE DATE(ave.viewed_at) BETWEEN ? AND ? " +
              "AND ave.article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY day ORDER BY day";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            if (ownerEmail != null) ps.setString(3, ownerEmail);
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

    public List<Map<String, Object>> getViewsMonthly(LocalDate from, LocalDate to, String ownerEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = ownerEmail == null
            ? "SELECT DATE_FORMAT(viewed_at, '%Y-%m') AS month, COUNT(*) AS views FROM article_view_event WHERE DATE(viewed_at) BETWEEN ? AND ? GROUP BY month ORDER BY month"
            : "SELECT DATE_FORMAT(ave.viewed_at, '%Y-%m') AS month, COUNT(*) AS views FROM article_view_event ave " +
              "WHERE DATE(ave.viewed_at) BETWEEN ? AND ? " +
              "AND ave.article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY month ORDER BY month";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            if (ownerEmail != null) ps.setString(3, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", rs.getString("month"));
                map.put("views", rs.getInt("views"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Map<String, Object>> getViewsHourly(LocalDate date, String ownerEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = ownerEmail == null
            ? "SELECT HOUR(viewed_at) as hr, COUNT(*) as views FROM article_view_event WHERE DATE(viewed_at) = ? GROUP BY hr ORDER BY hr"
            : "SELECT HOUR(ave.viewed_at) as hr, COUNT(*) as views FROM article_view_event ave " +
              "WHERE DATE(ave.viewed_at) = ? " +
              "AND ave.article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY hr ORDER BY hr";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            if (ownerEmail != null) ps.setString(2, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("date", String.format("%02d:00", rs.getInt("hr")));
                map.put("views", rs.getInt("views"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Map<String, Object>> getTopArticlesByViews(LocalDate from, LocalDate to, int limit, String ownerEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = ownerEmail == null
            ? "SELECT a.id, a.title, COUNT(ave.id) AS view_count FROM article a " +
              "LEFT JOIN article_view_event ave ON ave.article_id = a.id AND DATE(ave.viewed_at) BETWEEN ? AND ? " +
              "GROUP BY a.id, a.title ORDER BY view_count DESC LIMIT ?"
            : "SELECT a.id, a.title, COUNT(ave.id) AS view_count FROM article a " +
              "JOIN user u ON u.id = a.created_by_id AND LOWER(u.email) = LOWER(?) " +
              "LEFT JOIN article_view_event ave ON ave.article_id = a.id AND DATE(ave.viewed_at) BETWEEN ? AND ? " +
              "GROUP BY a.id, a.title ORDER BY view_count DESC LIMIT ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (ownerEmail == null) {
                ps.setDate(1, java.sql.Date.valueOf(from));
                ps.setDate(2, java.sql.Date.valueOf(to));
                ps.setInt(3, limit);
            } else {
                ps.setString(1, ownerEmail);
                ps.setDate(2, java.sql.Date.valueOf(from));
                ps.setDate(3, java.sql.Date.valueOf(to));
                ps.setInt(4, limit);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("title", rs.getString("title"));
                map.put("views", rs.getInt("view_count"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 3.1.1 Overview Stats
    public int getTotalPublishedArticles(String ownerEmail) {
        String sql = ownerEmail == null
            ? "SELECT COUNT(*) FROM article"
            : "SELECT COUNT(*) FROM article a JOIN user u ON u.id = a.created_by_id WHERE LOWER(u.email) = LOWER(?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (ownerEmail != null) ps.setString(1, ownerEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTotalComments(String ownerEmail) {
        String sql = ownerEmail == null
            ? "SELECT COUNT(*) FROM `comment`"
            : "SELECT COUNT(*) FROM `comment` WHERE article_id IN (" + NGO_ARTICLE_IDS + ")";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (ownerEmail != null) ps.setString(1, ownerEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public Map<String, Integer> getTotalReactions(String ownerEmail) {
        Map<String, Integer> result = new HashMap<>();
        String sql = ownerEmail == null
            ? "SELECT reaction as type, COUNT(*) as cnt FROM article_reaction_event GROUP BY type"
            : "SELECT reaction as type, COUNT(*) as cnt FROM article_reaction_event WHERE article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY type";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (ownerEmail != null) ps.setString(1, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String r = rs.getString("type");
                if (r != null) result.put(r.toUpperCase(), rs.getInt("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public Map<String, Integer> getReactionsByPeriod(LocalDate from, LocalDate to, String ownerEmail) {
        Map<String, Integer> result = new HashMap<>();
        String sql = ownerEmail == null
            ? "SELECT reaction as type, COUNT(*) as cnt FROM article_reaction_event WHERE DATE(acted_at) BETWEEN ? AND ? GROUP BY type"
            : "SELECT reaction as type, COUNT(*) as cnt FROM article_reaction_event " +
              "WHERE DATE(acted_at) BETWEEN ? AND ? " +
              "AND article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY type";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            if (ownerEmail != null) ps.setString(3, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String r = rs.getString("type");
                if (r != null) result.put(r.toUpperCase(), rs.getInt("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public int getViewsByPeriod(LocalDate from, LocalDate to, String ownerEmail) {
        String sql = ownerEmail == null
            ? "SELECT COUNT(*) FROM article_view_event WHERE DATE(viewed_at) BETWEEN ? AND ?"
            : "SELECT COUNT(*) FROM article_view_event ave WHERE DATE(ave.viewed_at) BETWEEN ? AND ? AND ave.article_id IN (" + NGO_ARTICLE_IDS + ")";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            if (ownerEmail != null) ps.setString(3, ownerEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getPublishedArticlesByPeriod(LocalDate from, LocalDate to, String ownerEmail) {
        String sql = ownerEmail == null
            ? "SELECT COUNT(*) FROM article WHERE DATE(published_at) BETWEEN ? AND ?"
            : "SELECT COUNT(*) FROM article a JOIN user u ON u.id = a.created_by_id WHERE DATE(a.published_at) BETWEEN ? AND ? AND LOWER(u.email) = LOWER(?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            if (ownerEmail != null) ps.setString(3, ownerEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getCommentsByPeriod(LocalDate from, LocalDate to, String ownerEmail) {
        String sql = ownerEmail == null
            ? "SELECT COUNT(*) FROM comment WHERE DATE(created_at) BETWEEN ? AND ?"
            : "SELECT COUNT(*) FROM comment c JOIN article a ON a.id = c.article_id JOIN user u ON u.id = a.created_by_id WHERE DATE(c.created_at) BETWEEN ? AND ? AND LOWER(u.email) = LOWER(?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            if (ownerEmail != null) ps.setString(3, ownerEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // Phase 3.1.2 Time-Series
    // Returns a 24-entry list (one per hour) with views, articles, comments, likes, dislikes
    public List<Map<String, Object>> getHourlyStats(LocalDate date, String ownerEmail) {
        int[] views = new int[24], articles = new int[24], comments = new int[24],
              likes = new int[24], dislikes = new int[24];

        String viewsSql = ownerEmail == null
            ? "SELECT HOUR(viewed_at) AS hr, COUNT(*) AS cnt FROM article_view_event WHERE DATE(viewed_at) = ? GROUP BY hr"
            : "SELECT HOUR(ave.viewed_at) AS hr, COUNT(*) AS cnt FROM article_view_event ave WHERE DATE(ave.viewed_at) = ? AND ave.article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY hr";
        try (PreparedStatement ps = cnx.prepareStatement(viewsSql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            if (ownerEmail != null) ps.setString(2, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) views[rs.getInt("hr")] = rs.getInt("cnt");
        } catch (SQLException e) { e.printStackTrace(); }

        String articlesSql = ownerEmail == null
            ? "SELECT HOUR(published_at) AS hr, COUNT(*) AS cnt FROM article WHERE DATE(published_at) = ? GROUP BY hr"
            : "SELECT HOUR(published_at) AS hr, COUNT(*) AS cnt FROM article WHERE DATE(published_at) = ? AND id IN (" + NGO_ARTICLE_IDS + ") GROUP BY hr";
        try (PreparedStatement ps = cnx.prepareStatement(articlesSql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            if (ownerEmail != null) ps.setString(2, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) articles[rs.getInt("hr")] = rs.getInt("cnt");
        } catch (SQLException e) { e.printStackTrace(); }

        String commentsSql = ownerEmail == null
            ? "SELECT HOUR(created_at) AS hr, COUNT(*) AS cnt FROM `comment` WHERE DATE(created_at) = ? GROUP BY hr"
            : "SELECT HOUR(c.created_at) AS hr, COUNT(*) AS cnt FROM `comment` c JOIN article a ON a.id = c.article_id JOIN user u ON u.id = a.created_by_id WHERE DATE(c.created_at) = ? AND LOWER(u.email) = LOWER(?) GROUP BY hr";
        try (PreparedStatement ps = cnx.prepareStatement(commentsSql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            if (ownerEmail != null) ps.setString(2, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) comments[rs.getInt("hr")] = rs.getInt("cnt");
        } catch (SQLException e) { e.printStackTrace(); }

        String reactionsSql = ownerEmail == null
            ? "SELECT HOUR(acted_at) AS hr, reaction, COUNT(*) AS cnt FROM article_reaction_event WHERE DATE(acted_at) = ? GROUP BY hr, reaction"
            : "SELECT HOUR(acted_at) AS hr, reaction, COUNT(*) AS cnt FROM article_reaction_event WHERE DATE(acted_at) = ? AND article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY hr, reaction";
        try (PreparedStatement ps = cnx.prepareStatement(reactionsSql)) {
            ps.setDate(1, java.sql.Date.valueOf(date));
            if (ownerEmail != null) ps.setString(2, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int hr = rs.getInt("hr");
                String r = rs.getString("reaction");
                if ("LIKE".equalsIgnoreCase(r)) likes[hr] = rs.getInt("cnt");
                else if ("DISLIKE".equalsIgnoreCase(r)) dislikes[hr] = rs.getInt("cnt");
            }
        } catch (SQLException e) { e.printStackTrace(); }

        List<Map<String, Object>> result = new ArrayList<>();
        for (int hr = 0; hr < 24; hr++) {
            Map<String, Object> map = new HashMap<>();
            map.put("hour", String.format("%02d:00", hr));
            map.put("views", views[hr]);
            map.put("articles", articles[hr]);
            map.put("comments", comments[hr]);
            map.put("likes", likes[hr]);
            map.put("dislikes", dislikes[hr]);
            result.add(map);
        }
        return result;
    }

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

    public List<Map<String, Object>> getViewsByHourOfDay(String ownerEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = ownerEmail == null
            ? "SELECT HOUR(viewed_at) AS hour_of_day, COUNT(*) AS views FROM article_view_event GROUP BY hour_of_day ORDER BY hour_of_day"
            : "SELECT HOUR(ave.viewed_at) AS hour_of_day, COUNT(*) AS views FROM article_view_event ave " +
              "WHERE ave.article_id IN (" + NGO_ARTICLE_IDS + ") GROUP BY hour_of_day ORDER BY hour_of_day";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (ownerEmail != null) ps.setString(1, ownerEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("hour", rs.getInt("hour_of_day"));
                map.put("views", rs.getInt("views"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    // Phase 3.1.5 Category & Tag Stats
    public List<Map<String, Object>> getCategoryStats(LocalDate from, LocalDate to, String ownerEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = ownerEmail == null
            ? "SELECT c.name, COUNT(DISTINCT a.id) AS article_count, COUNT(ave.id) AS total_views " +
              "FROM category c " +
              "LEFT JOIN article a ON a.category_id = c.id " +
              "LEFT JOIN article_view_event ave ON ave.article_id = a.id AND DATE(ave.viewed_at) BETWEEN ? AND ? " +
              "GROUP BY c.id, c.name ORDER BY total_views DESC"
            : "SELECT c.name, COUNT(DISTINCT a.id) AS article_count, COUNT(ave.id) AS total_views " +
              "FROM category c " +
              "LEFT JOIN article a ON a.category_id = c.id AND a.id IN (" + NGO_ARTICLE_IDS + ") " +
              "LEFT JOIN article_view_event ave ON ave.article_id = a.id AND DATE(ave.viewed_at) BETWEEN ? AND ? " +
              "GROUP BY c.id, c.name HAVING article_count > 0 ORDER BY total_views DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (ownerEmail == null) {
                ps.setDate(1, java.sql.Date.valueOf(from));
                ps.setDate(2, java.sql.Date.valueOf(to));
            } else {
                ps.setString(1, ownerEmail);
                ps.setDate(2, java.sql.Date.valueOf(from));
                ps.setDate(3, java.sql.Date.valueOf(to));
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", rs.getString("name"));
                map.put("articles", rs.getInt("article_count"));
                map.put("views", rs.getInt("total_views"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
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
        String sql = "SELECT COALESCE(u.username, u.email, 'Unknown') AS username, " +
                     "COUNT(DISTINCT a.id) as article_count, " +
                     "COUNT(ave.id) as total_views " +
                     "FROM user u " +
                     "JOIN article a ON a.created_by_id = u.id " +
                     "LEFT JOIN article_view_event ave ON ave.article_id = a.id " +
                     "AND DATE(ave.viewed_at) BETWEEN ? AND ? " +
                     "GROUP BY u.id, u.username, u.email ORDER BY total_views DESC";
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
        String sql = "SELECT t.name, COUNT(at.article_id) as article_count " +
                     "FROM tag t JOIN article_tag at ON t.id = at.tag_id " +
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
        String sql = "SELECT COALESCE(NULLIF(author_name,''), NULLIF(author,''), 'Anonymous') AS username, " +
                     "COUNT(id) as comment_count " +
                     "FROM `comment` " +
                     "GROUP BY username ORDER BY comment_count DESC LIMIT ?";
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
        String sql = "SELECT a.title, COALESCE(u.username, 'EcoSpot Contributor') as author, a.published_at, " +
                     "(SELECT COUNT(*) FROM article_view_event WHERE article_id = a.id) as views, " +
                     "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'LIKE') as likes, " +
                     "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'DISLIKE') as dislikes, " +
                     "(SELECT COUNT(*) FROM `comment` WHERE article_id = a.id) as comments " +
                     "FROM article a LEFT JOIN user u ON a.created_by_id = u.id WHERE a.id = ?";
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
                result.put("author", "EcoSpot Contributor");
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

    public List<Map<String, Object>> getAllArticlesWithStats() {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT a.id, a.title, COALESCE(u.username, 'EcoSpot Contributor') as author, " +
                     "(SELECT COUNT(*) FROM article_view_event WHERE article_id = a.id) as views, " +
                     "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'LIKE') as likes, " +
                     "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'DISLIKE') as dislikes, " +
                     "(SELECT COUNT(*) FROM `comment` WHERE article_id = a.id) as comments " +
                     "FROM article a LEFT JOIN user u ON a.created_by_id = u.id ORDER BY views DESC";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("title", rs.getString("title"));
                map.put("author", rs.getString("author"));
                map.put("views", rs.getInt("views"));
                map.put("likes", rs.getInt("likes"));
                map.put("dislikes", rs.getInt("dislikes"));
                map.put("comments", rs.getInt("comments"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Map<String, Object>> getArticleComments(int articleId) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT COALESCE(NULLIF(author_name,''), NULLIF(author,''), 'Anonymous') AS author, content, created_at FROM comment WHERE article_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("author", rs.getString("author"));
                map.put("content", rs.getString("content"));
                map.put("date", rs.getTimestamp("created_at"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public List<Map<String, Object>> getDetailedArticleStats(LocalDate from, LocalDate to, String ownerEmail) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = ownerEmail == null
            ? "SELECT a.id, a.title, a.published_at, COALESCE(u.username, 'EcoSpot Contributor') as author, " +
              "(SELECT COUNT(*) FROM article_view_event WHERE article_id = a.id AND DATE(viewed_at) BETWEEN ? AND ?) as views, " +
              "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'LIKE' AND DATE(acted_at) BETWEEN ? AND ?) as likes, " +
              "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'DISLIKE' AND DATE(acted_at) BETWEEN ? AND ?) as dislikes, " +
              "(SELECT COUNT(*) FROM comment WHERE article_id = a.id AND DATE(created_at) BETWEEN ? AND ?) as comments " +
              "FROM article a LEFT JOIN user u ON u.id = a.created_by_id"
            : "SELECT a.id, a.title, a.published_at, COALESCE(u.username, 'EcoSpot Contributor') as author, " +
              "(SELECT COUNT(*) FROM article_view_event WHERE article_id = a.id AND DATE(viewed_at) BETWEEN ? AND ?) as views, " +
              "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'LIKE' AND DATE(acted_at) BETWEEN ? AND ?) as likes, " +
              "(SELECT COUNT(*) FROM article_reaction_event WHERE article_id = a.id AND reaction = 'DISLIKE' AND DATE(acted_at) BETWEEN ? AND ?) as dislikes, " +
              "(SELECT COUNT(*) FROM comment WHERE article_id = a.id AND DATE(created_at) BETWEEN ? AND ?) as comments " +
              "FROM article a JOIN user u ON u.id = a.created_by_id WHERE LOWER(u.email) = LOWER(?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            if (ownerEmail == null) {
                ps.setDate(1, java.sql.Date.valueOf(from));
                ps.setDate(2, java.sql.Date.valueOf(to));
                ps.setDate(3, java.sql.Date.valueOf(from));
                ps.setDate(4, java.sql.Date.valueOf(to));
                ps.setDate(5, java.sql.Date.valueOf(from));
                ps.setDate(6, java.sql.Date.valueOf(to));
                ps.setDate(7, java.sql.Date.valueOf(from));
                ps.setDate(8, java.sql.Date.valueOf(to));
            } else {
                ps.setDate(1, java.sql.Date.valueOf(from));
                ps.setDate(2, java.sql.Date.valueOf(to));
                ps.setDate(3, java.sql.Date.valueOf(from));
                ps.setDate(4, java.sql.Date.valueOf(to));
                ps.setDate(5, java.sql.Date.valueOf(from));
                ps.setDate(6, java.sql.Date.valueOf(to));
                ps.setDate(7, java.sql.Date.valueOf(from));
                ps.setDate(8, java.sql.Date.valueOf(to));
                ps.setString(9, ownerEmail);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", rs.getInt("id"));
                map.put("title", rs.getString("title"));
                map.put("author", rs.getString("author"));
                map.put("published_at", rs.getTimestamp("published_at"));
                map.put("views", rs.getInt("views"));
                map.put("likes", rs.getInt("likes"));
                map.put("dislikes", rs.getInt("dislikes"));
                map.put("comments", rs.getInt("comments"));
                result.add(map);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }
}
