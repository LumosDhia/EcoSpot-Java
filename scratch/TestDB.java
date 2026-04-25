import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestDB {
    public static void main(String[] args) {
        String[] passwords = {"Wiem123456", "root", ""};
        for (String pwd : passwords) {
            try {
                System.out.println("Testing password: '" + pwd + "'");
                Connection cnx = DriverManager.getConnection("jdbc:mysql://localhost:3306/projetdev", "root", pwd);
                System.out.println("SUCCESS with password: '" + pwd + "'");
                cnx.close();
                return;
            } catch (SQLException e) {
                System.out.println("FAILED: " + e.getMessage());
            }
        }
    }
}
