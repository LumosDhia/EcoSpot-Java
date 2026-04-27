package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class CheckReactionDataTest {
    @Test
    public void checkData() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            System.out.println("--- article_reaction_event ---");
            ResultSet rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_reaction_event");
            if (rs.next()) System.out.println("Count: " + rs.getInt(1));
            
            rs = cnx.createStatement().executeQuery("SELECT reaction, acted_at FROM article_reaction_event LIMIT 5");
            while (rs.next()) {
                System.out.println("Reaction: " + rs.getString("reaction") + " | Date: " + rs.getTimestamp("acted_at"));
            }

            System.out.println("\n--- article_view_event ---");
            rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_view_event");
            if (rs.next()) System.out.println("Count: " + rs.getInt(1));
            
            rs = cnx.createStatement().executeQuery("SELECT viewed_at FROM article_view_event LIMIT 5");
            while (rs.next()) {
                System.out.println("Date: " + rs.getTimestamp("viewed_at"));
            }

            System.out.println("\n--- article_stats_daily ---");
            rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_stats_daily");
            if (rs.next()) System.out.println("Count: " + rs.getInt(1));
            
            rs = cnx.createStatement().executeQuery("SELECT stat_date, likes, dislikes FROM article_stats_daily LIMIT 5");
            while (rs.next()) {
                System.out.println("Date: " + rs.getDate("stat_date") + " | Likes: " + rs.getInt("likes") + " | Dislikes: " + rs.getInt("dislikes"));
            }

            System.out.println("\n--- article_reaction ---");
            rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_reaction");
            if (rs.next()) System.out.println("Count: " + rs.getInt(1));
            
            rs = cnx.createStatement().executeQuery("SELECT id FROM article_reaction LIMIT 5");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id"));
            }

            System.out.println("\n--- article_reaction_java ---");
            rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_reaction_java");
            if (rs.next()) System.out.println("Count: " + rs.getInt(1));
            
            rs = cnx.createStatement().executeQuery("SELECT type FROM article_reaction_java LIMIT 5");
            while (rs.next()) {
                System.out.println("Type: " + rs.getString("type"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
