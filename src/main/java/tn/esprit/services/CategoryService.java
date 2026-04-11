package tn.esprit.services;

import tn.esprit.blog.Category;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {

    Connection cnx = MyConnection.getInstance().getCnx();

    public List<Category> getAll() {
        List<Category> categories = new ArrayList<>();
        String req = "SELECT * FROM category";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }
}
