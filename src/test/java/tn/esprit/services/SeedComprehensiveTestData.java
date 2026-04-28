package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Random;

public class SeedComprehensiveTestData {

    @Test
    public void seedData() {
        Connection cnx = MyConnection.getInstance().getCnx();
        Random rand = new Random();
        
        try {
            // 1. Create Dummy Articles
            String insertArt = "INSERT INTO article (title, content, created_by_id, published_at, category_id) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psArt = cnx.prepareStatement(insertArt, Statement.RETURN_GENERATED_KEYS);
            
            int[] articleIds = new int[5];
            String[] titles = {"Ocean Cleanup Initiative", "Solar Energy Future", "Forest Restoration", "Urban Gardening Tips", "Sustainable Fashion"};
            
            for (int i = 0; i < 5; i++) {
                psArt.setString(1, titles[i]);
                psArt.setString(2, "Content for " + titles[i]);
                psArt.setInt(3, 1); // Assuming user 1 exists
                psArt.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now().minusMonths(rand.nextInt(12))));
                psArt.setInt(5, 1); // Assuming category 1 exists
                psArt.executeUpdate();
                
                ResultSet rs = psArt.getGeneratedKeys();
                if (rs.next()) articleIds[i] = rs.getInt(1);
            }
            
            // 2. Seed Views and Dislikes across periods
            String insertView = "INSERT INTO article_view_event (article_id, viewed_at) VALUES (?, ?)";
            String insertReact = "INSERT INTO article_reaction_event (article_id, reaction, acted_at) VALUES (?, ?, ?)";
            
            PreparedStatement psView = cnx.prepareStatement(insertView);
            PreparedStatement psReact = cnx.prepareStatement(insertReact);
            
            // PERIODS: Today (Hours), This Week (Days), This Month (Days), This Year (Months)
            LocalDateTime now = LocalDateTime.now();
            
            for (int artId : articleIds) {
                // TODAY (Hourly Views)
                for (int h = 0; h < 10; h++) {
                    psView.setInt(1, artId);
                    psView.setTimestamp(2, Timestamp.valueOf(now.withHour(rand.nextInt(now.getHour() + 1))));
                    psView.executeUpdate();
                    
                    if (rand.nextBoolean()) {
                        psReact.setInt(1, artId);
                        psReact.setString(2, rand.nextBoolean() ? "LIKE" : "DISLIKE");
                        psReact.setTimestamp(3, Timestamp.valueOf(now.withHour(rand.nextInt(now.getHour() + 1))));
                        psReact.executeUpdate();
                    }
                }
                
                // THIS WEEK (Daily Views)
                for (int d = 1; d < 7; d++) {
                    for (int v = 0; v < 5; v++) {
                        psView.setInt(1, artId);
                        psView.setTimestamp(2, Timestamp.valueOf(now.minusDays(d)));
                        psView.executeUpdate();
                    }
                    psReact.setInt(1, artId);
                    psReact.setString(2, "DISLIKE");
                    psReact.setTimestamp(3, Timestamp.valueOf(now.minusDays(d)));
                    psReact.executeUpdate();
                }
                
                // THIS MONTH (Daily Views)
                for (int d = 7; d < 30; d++) {
                    for (int v = 0; v < 3; v++) {
                        psView.setInt(1, artId);
                        psView.setTimestamp(2, Timestamp.valueOf(now.minusDays(d)));
                        psView.executeUpdate();
                    }
                }

                // THIS YEAR (Monthly Views)
                for (int m = 1; m < 12; m++) {
                    for (int v = 0; v < 20; v++) {
                        psView.setInt(1, artId);
                        psView.setTimestamp(2, Timestamp.valueOf(now.minusMonths(m)));
                        psView.executeUpdate();
                    }
                }
            }
            
            System.out.println("Successfully seeded 5 articles with hundreds of views and dislikes across all periods!");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
