package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GlobalRandomSeederTest {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        Random rand = new Random();
        try {
            // 1. Get all Article IDs
            List<Integer> articleIds = new ArrayList<>();
            ResultSet rsArt = cnx.createStatement().executeQuery("SELECT id FROM article");
            while (rsArt.next()) articleIds.add(rsArt.getInt(1));
            
            // 2. Get a valid User (assuming id is auto-increment int, and we need binary for comments)
            // Some tables use BINARY(16), some use INT.
            // I'll just use NULL for user_id in reactions if possible, or 1.
            // Let's try to find a valid user.
            int intUserId = 1;
            byte[] binUserId = null;
            ResultSet rsUser = cnx.createStatement().executeQuery("SELECT id FROM app_user LIMIT 1");
            if (rsUser.next()) {
                // If ID is returned as bytes or int
                Object obj = rsUser.getObject(1);
                if (obj instanceof byte[]) binUserId = (byte[]) obj;
                else if (obj instanceof Integer) intUserId = (Integer) obj;
            }
            
            // 3. Prepared Statements
            String reactionSql = "INSERT INTO article_reaction_event (article_id, user_id, reaction, acted_at) VALUES (?, ?, ?, DATE_SUB(NOW(), INTERVAL ? DAY))";
            String commentSql = "INSERT INTO comment (author, content, created_at, flagged, article_id, author_user_id, hidden_from_public) VALUES (?, ?, DATE_SUB(NOW(), INTERVAL ? DAY), ?, ?, ?, ?)";
            String viewSql = "INSERT INTO article_view_event (article_id, viewed_at, user_id) VALUES (?, DATE_SUB(NOW(), INTERVAL ? DAY), ?)";
            
            PreparedStatement psReact = cnx.prepareStatement(reactionSql);
            PreparedStatement psComm = cnx.prepareStatement(commentSql);
            PreparedStatement psView = cnx.prepareStatement(viewSql);
            
            int totalReacts = 0;
            int totalComments = 0;
            int totalViews = 0;
            
            for (int articleId : articleIds) {
                // Random Views (20-100)
                int views = rand.nextInt(81) + 20;
                for (int i = 0; i < views; i++) {
                    psView.setInt(1, articleId);
                    psView.setInt(2, rand.nextInt(30)); // random day in last 30
                    psView.setObject(3, null);
                    psView.executeUpdate();
                    totalViews++;
                }

                // Random Likes (5-15)
                int likes = rand.nextInt(11) + 5;
                for (int i = 0; i < likes; i++) {
                    psReact.setInt(1, articleId);
                    psReact.setObject(2, null); 
                    psReact.setString(3, "LIKE");
                    psReact.setInt(4, rand.nextInt(30));
                    psReact.executeUpdate();
                    totalReacts++;
                }
                
                // Random Dislikes (0-5)
                int dislikes = rand.nextInt(6);
                for (int i = 0; i < dislikes; i++) {
                    psReact.setInt(1, articleId);
                    psReact.setObject(2, null);
                    psReact.setString(3, "DISLIKE");
                    psReact.setInt(4, rand.nextInt(30));
                    psReact.executeUpdate();
                    totalReacts++;
                }
                
                // Random Comments (1-4)
                int commentsCount = rand.nextInt(4) + 1;
                for (int i = 0; i < commentsCount; i++) {
                    psComm.setString(1, "Guest " + rand.nextInt(100));
                    psComm.setString(2, "Random comment about article #" + articleId);
                    psComm.setInt(3, rand.nextInt(30));
                    psComm.setBoolean(4, false);
                    psComm.setInt(5, articleId);
                    psComm.setBytes(6, binUserId); 
                    psComm.setBoolean(7, false);
                    psComm.executeUpdate();
                    totalComments++;
                }
            }
            
            System.out.println("Global Seeding Complete!");
            System.out.println("Articles Processed: " + articleIds.size());
            System.out.println("Total Views Seeded: " + totalViews);
            System.out.println("Total Reactions Seeded: " + totalReacts);
            System.out.println("Total Comments Seeded: " + totalComments);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
