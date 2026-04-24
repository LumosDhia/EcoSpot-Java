package tn.esprit.util;
import java.sql.*;

public class DBUpdate {
    public static void main(String[] args) {
        try {
            Connection cnx = MyConnection.getInstance().getCnx();
            Statement st = cnx.createStatement();
            
            // 1. Fetch all users to update their usernames based on emails
            ResultSet rs = st.executeQuery("SELECT id, email, username FROM `user`");
            PreparedStatement psUpdateUser = cnx.prepareStatement("UPDATE `user` SET username = ? WHERE id = ?");
            PreparedStatement psUpdateComments = cnx.prepareStatement("UPDATE `comment` SET author = ? WHERE author = ?");
            
            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String oldUsername = rs.getString("username");
                
                if (email == null || !email.contains("@")) continue;
                
                // Extract name from email (e.g. john.doe@gmail.com -> John Doe)
                String localPart = email.split("@")[0];
                String newUsername = formatName(localPart);
                
                // Update user
                psUpdateUser.setString(1, newUsername);
                psUpdateUser.setInt(2, id);
                psUpdateUser.executeUpdate();
                
                // Update related comments so avatars don't break
                psUpdateComments.setString(1, newUsername);
                psUpdateComments.setString(2, oldUsername);
                psUpdateComments.executeUpdate();
                
                System.out.println("Updated: " + email + " -> " + newUsername);
                count++;
            }
            
            System.out.println("Successfully updated names for " + count + " users!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String formatName(String localPart) {
        // Replace dots, underscores, dashes with spaces
        String clean = localPart.replaceAll("[._-]", " ");
        // Capitalize words
        String[] words = clean.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
