package tn.esprit.scratch;

import tn.esprit.util.MyConnection;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FixArticleImages {

    private static final String FALLBACK_IMAGE_URL =
            "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Fronalpstock_big.jpg/1280px-Fronalpstock_big.jpg";

    private static final int HTTP_TIMEOUT_MS = 5000;

    private static final class ArticleRow {
        final int id;
        final String title;
        final String image;

        private ArticleRow(int id, String title, String image) {
            this.id = id;
            this.title = title;
            this.image = image;
        }
    }

    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("Database connection is not available.");
            return;
        }

        List<ArticleRow> articles = loadArticles(cnx);
        int checked = 0;
        int fixed = 0;

        for (ArticleRow article : articles) {
            checked++;
            if (!isValidImageReference(article.image)) {
                if (replaceWithFallback(cnx, article.id)) {
                    fixed++;
                    System.out.println("Fixed article #" + article.id + " (" + article.title + ")");
                }
            }
        }

        System.out.println("Checked: " + checked);
        System.out.println("Fixed: " + fixed);
        System.out.println("Fallback used: " + FALLBACK_IMAGE_URL);
    }

    private static List<ArticleRow> loadArticles(Connection cnx) {
        List<ArticleRow> rows = new ArrayList<>();
        String req = "SELECT id, title, image FROM article";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new ArticleRow(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("image")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static boolean replaceWithFallback(Connection cnx, int articleId) {
        String req = "UPDATE article SET image = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, FALLBACK_IMAGE_URL);
            ps.setInt(2, articleId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isValidImageReference(String imageRef) {
        if (imageRef == null || imageRef.trim().isEmpty()) {
            return false;
        }

        String value = imageRef.trim();
        try {
            if (value.startsWith("http://") || value.startsWith("https://")) {
                return checkHttpImage(value);
            }
            if (value.startsWith("file:/")) {
                File f = new File(URI.create(value));
                return f.exists() && f.isFile();
            }
            File relative = new File(value);
            if (!relative.isAbsolute()) {
                File workspaceRelative = new File(System.getProperty("user.dir"), value);
                return workspaceRelative.exists() && workspaceRelative.isFile();
            }
            return relative.exists() && relative.isFile();
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean checkHttpImage(String urlValue) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlValue);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setInstanceFollowRedirects(true);
            conn.setConnectTimeout(HTTP_TIMEOUT_MS);
            conn.setReadTimeout(HTTP_TIMEOUT_MS);
            int code = conn.getResponseCode();
            if (code < 200 || code >= 400) {
                return false;
            }
            String contentType = conn.getContentType();
            return contentType != null && contentType.toLowerCase().startsWith("image/");
        } catch (Exception e) {
            return false;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
