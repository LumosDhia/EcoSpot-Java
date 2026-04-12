package tn.esprit.services;

import tn.esprit.blog.Category;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {

    Connection cnx = MyConnection.getInstance().getCnx();

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String req = "SELECT * FROM category";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public Category createIfMissing(String name) {
        if (name == null) return null;
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return null;

        String selectReq = "SELECT id, name FROM category WHERE LOWER(name) = LOWER(?) LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(selectReq)) {
            ps.setString(1, trimmed);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Category(rs.getInt("id"), rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

        String insertReq = "INSERT INTO category (name) VALUES (?)";
        try (PreparedStatement ps = cnx.prepareStatement(insertReq, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trimmed);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return new Category(keys.getInt(1), trimmed);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int countArticlesForCategory(int categoryId) {
        String req = "SELECT COUNT(*) FROM article WHERE category_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, categoryId);
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

    public List<String> getArticleTitlesForCategory(int categoryId) {
        List<String> titles = new ArrayList<>();
        String req = "SELECT title FROM article WHERE category_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, categoryId);
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

    public boolean renameCategory(int categoryId, String newName) {
        String req = "UPDATE category SET name = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, newName);
            ps.setInt(2, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCategoryAndUnlinkArticles(int categoryId) {
        try {
            cnx.setAutoCommit(false);

            try (PreparedStatement unlink = cnx.prepareStatement("UPDATE article SET category_id = NULL WHERE category_id = ?")) {
                unlink.setInt(1, categoryId);
                unlink.executeUpdate();
            }

            int deleted;
            try (PreparedStatement delete = cnx.prepareStatement("DELETE FROM category WHERE id = ?")) {
                delete.setInt(1, categoryId);
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
}
