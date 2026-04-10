package tn.esprit.scratch;

import tn.esprit.util.MyConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class InitDatabase {
    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("Connection failed!");
            return;
        }

        try {
            Statement st = cnx.createStatement();
            
            // Create User table
            String createTableRes = "CREATE TABLE IF NOT EXISTS `user` (" +
                    "`id` INT AUTO_INCREMENT PRIMARY KEY," +
                    "`username` VARCHAR(100)," +
                    "`email` VARCHAR(150) UNIQUE," +
                    "`password` VARCHAR(255)," +
                    "`role` VARCHAR(20)" +
                    ")";
            st.execute(createTableRes);
            System.out.println("Table 'user' ensured.");

            // Add mock data (excluding hardcoded ones)
            String[] queries = {
                "INSERT IGNORE INTO `user` (`username`, `email`, `password`, `role`) VALUES ('dhia aouina', 'dhladhlaaouina@gmail.com', 'dhia123', 'USER')",
                "INSERT IGNORE INTO `user` (`username`, `email`, `password`, `role`) VALUES ('Alice Eco', 'alice@ecospot.local', 'user123', 'USER')",
                "INSERT IGNORE INTO `user` (`username`, `email`, `password`, `role`) VALUES ('Bob Green', 'bob@ecospot.local', 'user123', 'USER')",
                "INSERT IGNORE INTO `user` (`username`, `email`, `password`, `role`) VALUES ('Fan Eco 1', 'like0_1@test.com', 'test123', 'USER')",
                "INSERT IGNORE INTO `user` (`username`, `email`, `password`, `role`) VALUES ('Fan Eco 2', 'like0_2@test.com', 'test123', 'USER')"
            };

            for (String q : queries) {
                st.executeUpdate(q);
            }
            System.out.println("Mock data inserted.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
