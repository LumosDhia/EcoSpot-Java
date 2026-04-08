package tn.esprit.services;

import tn.esprit.blog.Comment;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentService {
    Connection cnx = MyConnection.getInstance().getCnx();

    public void add(Comment c) {
        String req = "INSERT INTO comment (author, content, created_at, article_id, author_id, flagged) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, c.getAuthor());
            ps.setString(2, c.getContent());
            ps.setTimestamp(3, Timestamp.valueOf(c.getCreatedAt()));
            ps.setInt(4, c.getArticleId());
            ps.setInt(5, c.getAuthorId());
            ps.setBoolean(6, c.isFlagged());
            ps.executeUpdate();
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
                Comment c = new Comment();
                c.setId(rs.getInt("id"));
                c.setAuthor(rs.getString("author"));
                c.setContent(rs.getString("content"));
                c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                c.setArticleId(rs.getInt("article_id"));
                c.setAuthorId(rs.getInt("author_id"));
                c.setFlagged(rs.getBoolean("flagged"));
                comments.add(c);
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
