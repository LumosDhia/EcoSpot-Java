package tn.esprit.util;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GeocodeService {

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search?format=json&limit=5&q=";
    private final HttpClient httpClient;

    public GeocodeService() {
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public CompletableFuture<List<String>> searchCities(String query) {
        if (query == null || query.trim().length() < 3) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        String encodedQuery = java.net.URLEncoder.encode(query.trim(), java.nio.charset.StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(NOMINATIM_URL + encodedQuery))
                .header("User-Agent", "EcoSpot-Java/1.0 (Environmental App)")
                .header("Accept", "application/json")
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("Nominatim Response Code: " + response.statusCode());
                    List<String> cities = new ArrayList<>();
                    if (response.statusCode() == 200) {
                        System.out.println("Nominatim Body: " + response.body());
                        JSONArray array = new JSONArray(response.body());
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject obj = array.getJSONObject(i);
                            cities.add(obj.getString("display_name"));
                        }
                    }
                    return cities;
                })
                .exceptionally(ex -> {
                    System.err.println("Nominatim Error: " + ex.getMessage());
                    ex.printStackTrace();
                    return new ArrayList<>();
                });
    }
}
