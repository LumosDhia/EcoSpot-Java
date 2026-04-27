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

public class FixTicketImages {

    private static final String FALLBACK_TICKET_IMAGE_URL =
            "https://upload.wikimedia.org/wikipedia/commons/thumb/6/6f/Street_in_City.jpg/1280px-Street_in_City.jpg";
    private static final String FALLBACK_COMPLETION_IMAGE_URL =
            "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3f/Fronalpstock_big.jpg/1280px-Fronalpstock_big.jpg";
    private static final int HTTP_TIMEOUT_MS = 5000;

    private static final class TicketRow {
        final int id;
        final String title;
        final String image;
        final String completionImage;

        private TicketRow(int id, String title, String image, String completionImage) {
            this.id = id;
            this.title = title;
            this.image = image;
            this.completionImage = completionImage;
        }
    }

    public static void main(String[] args) {
        Connection cnx = MyConnection.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("Database connection is not available.");
            return;
        }

        List<TicketRow> tickets = loadTickets(cnx);
        int checked = 0;
        int fixedMain = 0;
        int fixedCompletion = 0;

        for (TicketRow ticket : tickets) {
            checked++;
            boolean updateMain = !isValidImageReference(ticket.image);
            boolean updateCompletion = ticket.completionImage != null
                    && !ticket.completionImage.trim().isEmpty()
                    && !isValidImageReference(ticket.completionImage);

            if (updateMain || updateCompletion) {
                if (updateTicketImages(
                        cnx,
                        ticket.id,
                        updateMain ? FALLBACK_TICKET_IMAGE_URL : ticket.image,
                        updateCompletion ? FALLBACK_COMPLETION_IMAGE_URL : ticket.completionImage
                )) {
                    if (updateMain) fixedMain++;
                    if (updateCompletion) fixedCompletion++;
                    System.out.println("Fixed ticket #" + ticket.id + " (" + ticket.title + ")");
                }
            }
        }

        System.out.println("Checked tickets: " + checked);
        System.out.println("Fixed main image: " + fixedMain);
        System.out.println("Fixed completion image: " + fixedCompletion);
    }

    private static List<TicketRow> loadTickets(Connection cnx) {
        List<TicketRow> rows = new ArrayList<>();
        String req = "SELECT id, title, image, completion_image FROM ticket";
        try (PreparedStatement ps = cnx.prepareStatement(req);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rows.add(new TicketRow(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("image"),
                        rs.getString("completion_image")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static boolean updateTicketImages(Connection cnx, int ticketId, String image, String completionImage) {
        String req = "UPDATE ticket SET image = ?, completion_image = ? WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(req)) {
            ps.setString(1, image);
            ps.setString(2, completionImage);
            ps.setInt(3, ticketId);
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
