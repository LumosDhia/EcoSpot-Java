package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDate;

public class FinalArticleDebugTest {
    @Test
    public void debug() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            LocalDate today = LocalDate.now();
            System.out.println("Searching for articles published on: " + today);
            
            String sql = "SELECT id, title, published_at, DATE(published_at) as pub_date FROM article WHERE DATE(published_at) = ?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setDate(1, java.sql.Date.valueOf(today));
            ResultSet rs = ps.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("Match found: ID=" + rs.getInt("id") + " | Title=" + rs.getString("title") + " | TS=" + rs.getTimestamp("published_at") + " | DATE()=" + rs.getString("pub_date"));
            }
            System.out.println("Total matches found for " + today + ": " + count);
            
            System.out.println("FULL ARTICLE TABLE DUMP:");
            ResultSet rs3 = cnx.createStatement().executeQuery("SELECT id, title, published_at FROM article");
            while (rs3.next()) {
                System.out.println(" - ID: " + rs3.getInt("id") + " | " + rs3.getString("title") + " | Pub: " + rs3.getTimestamp("published_at"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
