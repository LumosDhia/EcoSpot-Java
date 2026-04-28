package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class FinalArticleDiagnosticTest {
    @Test
    public void debug() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            System.out.println("--- CHECKING ALL ARTICLES CREATED TODAY ---");
            String sql = "SELECT a.id, a.title, a.published_at, HEX(a.created_by_id) as creator_hex, u.email " +
                         "FROM article a " +
                         "LEFT JOIN app_user u ON u.id = a.created_by_id " +
                         "WHERE DATE(a.published_at) = CURDATE()";
            ResultSet rs = cnx.createStatement().executeQuery(sql);
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + " | Title: " + rs.getString("title") + " | CreatorHex: " + rs.getString("creator_hex") + " | Email: " + rs.getString("email"));
            }
            
            System.out.println("--- CHECKING NGO_ARTICLE_IDS FILTER ---");
            // If we know the email, we can test it. Let's try to guess the email from the most active creator.
            ResultSet rs2 = cnx.createStatement().executeQuery("SELECT u.email, COUNT(*) as cnt FROM article a JOIN app_user u ON u.id = a.created_by_id GROUP BY u.email ORDER BY cnt DESC LIMIT 1");
            if (rs2.next()) {
                String email = rs2.getString("email");
                System.out.println("Testing filter for email: " + email);
                String sql3 = "SELECT COUNT(*) FROM article a JOIN app_user u ON u.id = a.created_by_id WHERE DATE(a.published_at) = CURDATE() AND LOWER(u.email) = LOWER(?)";
                PreparedStatement ps = cnx.prepareStatement(sql3);
                ps.setString(1, email);
                ResultSet rs3 = ps.executeQuery();
                if (rs3.next()) System.out.println("Count for " + email + ": " + rs3.getInt(1));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
