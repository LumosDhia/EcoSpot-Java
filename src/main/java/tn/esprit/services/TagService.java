package tn.esprit.services;

import tn.esprit.blog.Tag;
import tn.esprit.util.MyConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagService {

    Connection cnx = MyConnection.getInstance().getCnx();

    public List<Tag> getAll() {
        List<Tag> tags = new ArrayList<>();
        String req = "SELECT * FROM tag";
        try (Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(req)) {
            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tags;
    }
}
