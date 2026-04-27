package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SeedExistingArticlesTest {
    @Test
    public void seed() {
        Connection cnx = MyConnection.getInstance().getCnx();
        Random rand = new Random();
        try {
            // Find 5 articles
            List<Integer> ids = new ArrayList<>();
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id FROM article LIMIT 5");
            while (rs.next()) ids.add(rs.getInt("id"));
            
            if (ids.isEmpty()) {
                System.out.println("No articles found! Creating one...");
                // Just in case, create one
                PreparedStatement ps = cnx.prepareStatement("INSERT INTO article (title, content, created_by_id, category_id) VALUES ('Test Article', 'Content', 1, 1)", Statement.RETURN_GENERATED_KEYS);
                ps.executeUpdate();
                ResultSet rs2 = ps.getGeneratedKeys();
                if (rs2.next()) ids.add(rs2.getInt(1));
            }
            
            String insView = "INSERT INTO article_view_event (article_id, viewed_at) VALUES (?, ?)";
            String insReact = "INSERT INTO article_reaction_event (article_id, reaction, acted_at) VALUES (?, ?, ?)";
            PreparedStatement psV = cnx.prepareStatement(insView);
            PreparedStatement psR = cnx.prepareStatement(insReact);
            
            LocalDateTime now = LocalDateTime.now();
            for (int id : ids) {
                // Seed 20 views for TODAY
                for (int i = 0; i < 20; i++) {
                    psV.setInt(1, id);
                    psV.setTimestamp(2, Timestamp.valueOf(now.withHour(rand.nextInt(24)).withMinute(rand.nextInt(60))));
                    psV.executeUpdate();
                }
                // Seed 5 DISLIKES for TODAY
                for (int i = 0; i < 5; i++) {
                    psR.setInt(1, id);
                    psR.setString(2, "DISLIKE");
                    psR.setTimestamp(3, Timestamp.valueOf(now.withHour(rand.nextInt(24))));
                    psR.executeUpdate();
                }
            }
            System.out.println("Seeded " + (ids.size() * 20) + " views for TODAY across " + ids.size() + " articles.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
