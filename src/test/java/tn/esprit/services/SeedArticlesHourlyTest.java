package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class SeedArticlesHourlyTest {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            String sql = "INSERT INTO article (title, content, created_by_id, category_id, published_at, writer_id, status) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = cnx.prepareStatement(sql);
            
            LocalDateTime now = LocalDateTime.now();
            for (int h = 0; h < 12; h++) {
                int hour = (h * 2) % 24; // Spread across 24 hours
                ps.setString(1, "Hourly Post @ " + hour + ":00");
                ps.setString(2, "Content...");
                ps.setInt(3, 1);
                ps.setInt(4, 1);
                ps.setTimestamp(5, Timestamp.valueOf(now.withHour(hour).withMinute(0)));
                ps.setInt(6, 1);
                ps.setString(7, "PUBLISHED");
                ps.executeUpdate();
            }
            
            System.out.println("Published 12 articles spread across different hours of today.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
