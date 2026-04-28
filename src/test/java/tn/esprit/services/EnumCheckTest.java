package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class EnumCheckTest {
    @Test
    public void checkEnum() {
        Connection cnx = MyConnection.getInstance().getCnx();
        try {
            ResultSet rs = cnx.createStatement().executeQuery("SELECT COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_NAME = 'article_reaction_event' AND COLUMN_NAME = 'reaction'");
            if (rs.next()) {
                System.out.println("Enum Values: " + rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
