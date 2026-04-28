package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;
import java.time.LocalDateTime;

public class CombinedInsertSelectTest {
    @Test
    public void test() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            String title = "COMBINED_TEST_" + System.currentTimeMillis();
            String sql = "INSERT INTO article (title, content, created_by_id, category_id, published_at, writer_id, status) VALUES (?, 'Content', 1, 1, ?, 1, 'PUBLISHED')";
            PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, title);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            int rows = ps.executeUpdate();
            
            System.out.println("Insert rows: " + rows);
            
            ResultSet rsKeys = ps.getGeneratedKeys();
            if (rsKeys.next()) {
                int newId = rsKeys.getInt(1);
                System.out.println("Generated ID: " + newId);
                
                // Immediately select it
                PreparedStatement psSel = cnx.prepareStatement("SELECT * FROM article WHERE id = ?");
                psSel.setInt(1, newId);
                ResultSet rsArt = psSel.executeQuery();
                if (rsArt.next()) {
                    System.out.println("SUCCESSFULLY SELECTED: " + rsArt.getString("title"));
                } else {
                    System.out.println("FAILED TO SELECT JUST-INSERTED ARTICLE!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
