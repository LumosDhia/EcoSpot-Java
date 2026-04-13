package tn.esprit.services;

import tn.esprit.blog.Tag;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagService {

    Connection cnx = MyConnection.getInstance().getCnx();
    private static final int MIN_NAME_LENGTH = 2;
    private static final int MAX_NAME_LENGTH = 30;

    public List<Tag> getAll() {
        List<Tag> tags = new ArrayList<>();
        String req = "SELECT * FROM tag";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }

    public Tag createIfMissing(String name) {
        String trimmed = normalizeTagName(name);
        if (!isValidTagName(trimmed)) return null;

        String selectReq = "SELECT id, name FROM tag WHERE LOWER(name) = LOWER(?) LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(selectReq)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Tag(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        String insertReq = "INSERT INTO tag (name) VALUES (?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertReq, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trimmed);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Tag(keys.getInt(1), trimmed);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countArticlesForTag(int tagId) {
        String req = "SELECT COUNT(*) FROM article_tag WHERE tag_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, tagId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<String> getArticleTitlesForTag(int tagId) {
        List<String> titles = new ArrayList<>();
        String req = "SELECT a.title FROM article a JOIN article_tag at ON a.id = at.article_id WHERE at.tag_id = ? ORDER BY a.created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, tagId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    titles.add(rs.getString("title"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return titles;
    }

    public boolean renameTag(int tagId, String newName) {
        String normalized = normalizeTagName(newName);
        if (!isValidTagName(normalized)) {
            return false;
        }
        String req = "UPDATE tag SET name = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, normalized);
            ps.setInt(2, tagId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTagAndUnlinkArticles(int tagId) {
        try {
            cnx.setAutoCommit(false);

            try (PreparedStatement unlink = cnx.prepareStatement("DELETE FROM article_tag WHERE tag_id = ?")) {
                unlink.setInt(1, tagId);
                unlink.executeUpdate();
            }

            int deleted;
            try (PreparedStatement delete = cnx.prepareStatement("DELETE FROM tag WHERE id = ?")) {
                delete.setInt(1, tagId);
                deleted = delete.executeUpdate();
            }

            cnx.commit();
            return deleted > 0;
        } catch (SQLException e) {
            try {
                cnx.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                cnx.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    private String normalizeTagName(String input) {
        return input == null ? "" : input.trim().replaceAll("\\s+", " ");
    }

    private boolean isValidTagName(String name) {
        if (name.isEmpty()) return false;
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) return false;
        if (!name.matches("^[A-Za-z].*")) return false;
        return name.matches("^[A-Za-z][A-Za-z0-9\\s\\-_#&]*$");
    }
}
