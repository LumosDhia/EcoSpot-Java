package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class SeedCommentsTest {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            // 1. Find today's articles
            String findSql = "SELECT id FROM article WHERE DATE(published_at) = CURDATE()";
            ResultSet rs = cnx.createStatement().executeQuery(findSql);
            
            // 2. Find a valid user ID (binary)
            byte[] userId = null;
            ResultSet rsUser = cnx.createStatement().executeQuery("SELECT id FROM user LIMIT 1");
            if (rsUser.next()) userId = rsUser.getBytes(1);
            
            String insertSql = "INSERT INTO comment (author, content, created_at, flagged, article_id, author_user_id, hidden_from_public) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(insertSql);
            
            int count = 0;
            while (rs.next()) {
                int articleId = rs.getInt(1);
                
                // Add 2 comments per article
                for (int i = 1; i <= 2; i++) {
                    ps.setString(1, "User " + i);
                    ps.setString(2, "Great article on today's topic! (Comment #" + i + ")");
                    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setBoolean(4, false);
                    ps.setInt(5, articleId);
                    ps.setBytes(6, userId);
                    ps.setBoolean(7, false);
                    ps.executeUpdate();
                    count++;
                }
            }
            System.out.println("Successfully seeded " + count + " comments for today's articles.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
