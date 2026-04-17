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

public class UnsplashService {
    // NOTE: Keep this placeholder for the user to fill or I'll provide a working one if I had it.
    private static final String ACCESS_KEY = "YOUR_UNSPLASH_ACCESS_KEY"; 
    private static final String API_URL = "https://api.unsplash.com/search/photos";

    public List<String> searchPhotos(String query) {
        List<String> imageUrls = new ArrayList<>();
        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8.toString());
            String urlString = API_URL + "?query=" + encodedQuery + "&per_page=9&client_id=" + ACCESS_KEY;
            
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject photo = results.getJSONObject(i);
                    imageUrls.add(photo.getJSONObject("urls").getString("regular"));
                }
            } else {
                System.out.println("Unsplash API Error: " + conn.getResponseCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imageUrls;
    }
}
