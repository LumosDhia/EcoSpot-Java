package tn.esprit;

import tn.esprit.services.StatisticsService;
import tn.esprit.util.StatisticsCollector;
import tn.esprit.util.MyConnection;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDate;

public class TestStatistics {
    public static void main(String[] args) {
        System.out.println("Starting Test...");
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            Statement st = cnx.createStatement();
            
            // Ensure Tables
            System.out.println("Creating tables...");
            st.execute("CREATE TABLE IF NOT EXISTS `article_view_event` (" +
                "`id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`article_id` int(11) NOT NULL," +
                "`user_id` int(11) DEFAULT NULL," +
                "`session_id` varchar(64) DEFAULT NULL," +
                "`viewed_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
                
            st.execute("CREATE TABLE IF NOT EXISTS `article_reaction_event` (" +
                "`id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`article_id` int(11) NOT NULL," +
                "`user_id` int(11) DEFAULT NULL," +
                "`reaction` enum('LIKE','DISLIKE') NOT NULL," +
                "`acted_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
                
            st.execute("CREATE TABLE IF NOT EXISTS `search_term_log` (" +
                "`id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`term` varchar(255) NOT NULL," +
                "`result_count` int(11) NOT NULL DEFAULT 0," +
                "`searched_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
                
            st.execute("CREATE TABLE IF NOT EXISTS `article_stats_daily` (" +
                "`id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`article_id` int(11) NOT NULL," +
                "`stat_date` date NOT NULL," +
                "`views` int(11) NOT NULL DEFAULT 0," +
                "`likes` int(11) NOT NULL DEFAULT 0," +
                "`dislikes` int(11) NOT NULL DEFAULT 0," +
                "`comments` int(11) NOT NULL DEFAULT 0," +
                "PRIMARY KEY (`id`)," +
                "UNIQUE KEY `uq_article_date` (`article_id`,`stat_date`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            
            System.out.println("Tables verified!");

            // Test 1: Record View
            System.out.println("Recording view...");
            StatisticsCollector.getInstance().recordView(1, "test-session-123", 1);
            System.out.println("View recorded.");

            // Test 2: Record Search
            System.out.println("Recording search term...");
            StatisticsCollector.getInstance().recordSearchTerm("eco", 5);
            System.out.println("Search term recorded.");

            // Test 3: Get Total Views
            StatisticsService statService = new StatisticsService();
            int totalViews = statService.getTotalViews(LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
            System.out.println("Total views in range: " + totalViews);

            System.out.println("Test Complete Successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
