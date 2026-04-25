package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class GeocodingService {
    private static final String API_URL = "https://nominatim.openstreetmap.org/search";

    public static class Place {
        private String displayName;
        private double lat;
        private double lon;

        public Place(String displayName, double lat, double lon) {
            this.displayName = displayName;
            this.lat = lat;
            this.lon = lon;
        }

        public String getDisplayName() { return displayName; }
        public double getLat() { return lat; }
        public double getLon() { return lon; }

        @Override
        public String toString() { return displayName; }
    }

    public List<Place> search(String query) {
        List<Place> places = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String urlString = API_URL + "?q=" + encodedQuery + "&format=json&limit=5";
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            // Nominatim requires a User-Agent
            conn.setRequestProperty("User-Agent", "EcoSpot-Java-App");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray results = new JSONArray(response.toString());
                for (int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    places.add(new Place(
                        obj.getString("display_name"),
                        obj.getDouble("lat"),
                        obj.getDouble("lon")
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return places;
    }
}
