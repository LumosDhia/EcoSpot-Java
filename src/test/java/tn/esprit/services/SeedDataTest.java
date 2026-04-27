package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.util.Random;

public class SeedDataTest {

    @Test
    public void seedStatsData() {
        Connection cnx = MyConnection.getInstance().getCnx();
        Random random = new Random();

        try {
            // Get valid article IDs
            java.util.List<Integer> articleIds = new java.util.ArrayList<>();
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id FROM article");
            while (rs.next()) articleIds.add(rs.getInt("id"));

            if (articleIds.isEmpty()) {
                System.out.println("No articles found to seed views for.");
                return;
            }

            // Seed Views for the last 14 days
            String viewSql = "INSERT INTO article_view_event (article_id, session_id, viewed_at) VALUES (?, ?, DATE_SUB(NOW(), INTERVAL ? HOUR))";
            PreparedStatement psView = cnx.prepareStatement(viewSql);
            for (int i = 0; i < 100; i++) {
                psView.setInt(1, articleIds.get(random.nextInt(articleIds.size())));
                psView.setString(2, "session_" + random.nextInt(1000));
                psView.setInt(3, random.nextInt(336)); // Random hour in last 14 days
                psView.executeUpdate();
            }

            // Force articles to 'published' status for testing
            cnx.createStatement().executeUpdate("UPDATE article SET status = 'published' WHERE status IS NULL OR status = ''");
            
            // Seed Search Terms
            String searchSql = "INSERT INTO search_term_log (term, result_count, searched_at) VALUES (?, ?, DATE_SUB(NOW(), INTERVAL ? MINUTE))";
            PreparedStatement psSearch = cnx.prepareStatement(searchSql);
            String[] terms = {"ecology", "recycling", "water", "forest", "solar", "nature", "green", "energy"};
            for (int i = 0; i < 30; i++) {
                psSearch.setString(1, terms[random.nextInt(terms.length)]);
                psSearch.setInt(2, random.nextInt(15));
                psSearch.setInt(3, random.nextInt(10000));
                psSearch.executeUpdate();
            }

            // Seed Reactions
            String reactionSql = "INSERT INTO article_reaction_event (article_id, user_id, reaction, acted_at) VALUES (?, ?, ?, NOW())";
            PreparedStatement psReaction = cnx.prepareStatement(reactionSql);
            for (int i = 0; i < 40; i++) {
                psReaction.setInt(1, articleIds.get(random.nextInt(articleIds.size())));
                psReaction.setInt(2, 1); // Assuming user ID 1 exists
                psReaction.setString(3, random.nextBoolean() ? "LIKE" : "DISLIKE");
                psReaction.executeUpdate();
            }

            System.out.println("Real dummy statistics data seeded successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
