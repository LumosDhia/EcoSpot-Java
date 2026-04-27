package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class FoolproofArticleSeeder {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            // 1. Fetch valid writer_id and created_by_id (BINARY/UUID) from an existing article
            byte[] writerId = null;
            byte[] createdById = null;
            int categoryId = 1;
            
            ResultSet rsSource = cnx.createStatement().executeQuery("SELECT writer_id, created_by_id, category_id FROM article WHERE writer_id IS NOT NULL LIMIT 1");
            if (rsSource.next()) {
                writerId = rsSource.getBytes("writer_id");
                createdById = rsSource.getBytes("created_by_id");
                categoryId = rsSource.getInt("category_id");
            } else {
                System.out.println("No articles found to copy writer/creator from!");
                return;
            }
            
            // 2. Insert new articles with TODAY's timestamp
            String sql = "INSERT INTO article (title, content, created_by_id, writer_id, category_id, published_at, created_at) VALUES (?, 'Content', ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            
            String[] titles = {"Today's Discovery", "Hourly Update: Morning", "Hourly Update: Afternoon"};
            int[] hours = {9, 14, 20};
            
            LocalDateTime now = LocalDateTime.now();
            for (int i = 0; i < titles.length; i++) {
                ps.setString(1, titles[i]);
                ps.setBytes(2, createdById);
                ps.setBytes(3, writerId);
                ps.setInt(4, categoryId);
                ps.setTimestamp(5, Timestamp.valueOf(now.withHour(hours[i])));
                ps.setTimestamp(6, Timestamp.valueOf(now.withHour(hours[i])));
                ps.executeUpdate();
            }
            
            cnx.commit();
            System.out.println("FOOLPROOF: Successfully published 3 articles for TODAY and COMMITTED.");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
