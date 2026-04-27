package tn.esprit.tests;

import tn.esprit.util.MyConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class SeedData {
    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance().getCnx();
        Random random = new Random();

        try {
            // Seed Views for the last 7 days
            String viewSql = "INSERT INTO article_view_event (article_id, session_id, viewed_at) VALUES (?, ?, DATE_SUB(NOW(), INTERVAL ? HOUR))";
            PreparedStatement psView = cnx.prepareStatement(viewSql);
            for (int i = 0; i < 50; i++) {
                psView.setInt(1, random.nextInt(5) + 1); // Assuming article IDs 1-5
                psView.setString(2, "session_" + random.nextInt(1000));
                psView.setInt(3, random.nextInt(168)); // Random hour in last week
                psView.executeUpdate();
            }

            // Seed Search Terms
            String searchSql = "INSERT INTO search_term_log (term, result_count, searched_at) VALUES (?, ?, NOW())";
            PreparedStatement psSearch = cnx.prepareStatement(searchSql);
            String[] terms = {"ecology", "recycling", "water", "forest", "solar"};
            for (String term : terms) {
                psSearch.setString(1, term);
                psSearch.setInt(2, random.nextInt(10));
                psSearch.executeUpdate();
            }

            // Seed Reactions
            String reactionSql = "INSERT INTO article_reaction_event (article_id, reaction, reacted_at) VALUES (?, ?, NOW())";
            PreparedStatement psReaction = cnx.prepareStatement(reactionSql);
            for (int i = 0; i < 20; i++) {
                psReaction.setInt(1, random.nextInt(5) + 1);
                psReaction.setString(2, random.nextBoolean() ? "LIKE" : "DISLIKE");
                psReaction.executeUpdate();
            }

            System.out.println("Dummy statistics data seeded successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
