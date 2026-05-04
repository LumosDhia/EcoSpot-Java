package tn.esprit.services;

import org.junit.jupiter.api.Test;
import tn.esprit.util.MyConnection;
import java.sql.*;

public class CommentDiagnosticTest {
    @Test
    public void runDiagnostic() throws SQLException {
        Connection cnx = MyConnection.getInstance().getCnx();
        ResultSet rs = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM comment WHERE DATE(created_at) < CURDATE()");
        if (rs.next()) System.out.println(">>> COMMENTS BEFORE TODAY: " + rs.getInt(1));
        
        ResultSet rs2 = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_reaction_event WHERE DATE(acted_at) < CURDATE()");
        if (rs2.next()) System.out.println(">>> REACTIONS BEFORE TODAY: " + rs2.getInt(1));

        ResultSet rs3 = cnx.createStatement().executeQuery("SELECT COUNT(*) FROM article_view_event WHERE DATE(viewed_at) < CURDATE()");
        if (rs3.next()) System.out.println(">>> VIEWS BEFORE TODAY: " + rs3.getInt(1));
    }
}
