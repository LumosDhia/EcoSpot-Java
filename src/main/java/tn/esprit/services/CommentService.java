package tn.esprit.services;

import tn.esprit.blog.Comment;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {
    Connection cnx = MyConnection.getInstance().getCnx();

    public void add(Comment c) {
        String req = "INSERT INTO comment (article_id, content, created_at, author_name) VALUES (?, ?, NOW(), ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, c.getArticleId());
            ps.setString(2, c.getContent());
            ps.setString(3, c.getAuthorName() != null ? c.getAuthorName() : "Anonymous User");
            ps.executeUpdate();
            System.out.println("Comment added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Comment> getByArticleId(int articleId) {
        List<Comment> comments = new ArrayList<>();
        String req = "SELECT * FROM comment WHERE article_id = ? ORDER BY created_at DESC";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, articleId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                comments.add(new Comment(
                    rs.getInt("id"),
                    rs.getInt("article_id"),
                    rs.getString("author_name"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    public void delete(int id) {
        String req = "DELETE FROM comment WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
