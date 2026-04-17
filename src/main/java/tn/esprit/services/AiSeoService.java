package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AiSeoService {

    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "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";

    public static class SeoResult {
        public String title;
        public String description;
        public String keywords;

        public SeoResult(String title, String description, String keywords) {
            this.title = title;
            this.description = description;
            this.keywords = keywords;
        }
    }

    public CompletableFuture<SeoResult> generateSeoElements(String articleTitle, String articleContent) {
        String plainContent = articleContent.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
        String contentChunk = plainContent.length() > 3000 ? plainContent.substring(0, 3000) : plainContent;

        String prompt = String.format(
            "You are a content expert. Analyze the following article content to generate high-quality metadata.\n" +
            "Reply ONLY with a JSON object containing carefully crafted:\n" +
            "- \"title\": A catchy title (max 60 chars).\n" +
            "- \"description\": A compelling summary description (max 160 chars).\n" +
            "- \"keywords\": A comma-separated list of 5-10 relevant keywords.\n\n" +
            "Article Title: %s\n" +
            "Article Content: %s",
            articleTitle, contentChunk
        );

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("model", "openrouter/auto");
        
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", "You are an SEO assistant. You must output ONLY valid JSON."));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        
        jsonPayload.put("messages", messages);
        jsonPayload.put("response_format", new JSONObject().put("type", "json_object"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost:8000")
                .header("X-Title", "EcoSpot Java")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        System.err.println("OpenRouter error: " + response.body());
                        return null;
                    }
                    try {
                        JSONObject data = new JSONObject(response.body());
                        String content = data.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                        JSONObject seo = new JSONObject(content);
                        return new SeoResult(
                            seo.getString("title"),
                            seo.getString("description"),
                            seo.getString("keywords")
                        );
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                });
    }

    public CompletableFuture<List<String>> generateTitleIdeas(String articleTitle, String articleContent) {
        String plainContent = articleContent.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ");
        String contentChunk = plainContent.length() > 3000 ? plainContent.substring(0, 3000) : plainContent;

        String prompt = String.format(
            "You are a creative editor. Analyze the following article content to generate 5 DIFFERENT catchy and engaging titles (max 60 chars each).\n" +
            "Reply ONLY with a JSON object containing a key \"titles\" which is an array of 5 strings.\n\n" +
            "Article Title: %s\n" +
            "Article Content: %s",
            articleTitle, contentChunk
        );

        JSONObject jsonPayload = new JSONObject();
        jsonPayload.put("model", "openrouter/auto");
        
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "system").put("content", "You are an SEO assistant. You must output ONLY valid JSON."));
        messages.put(new JSONObject().put("role", "user").put("content", prompt));
        
        jsonPayload.put("messages", messages);
        jsonPayload.put("response_format", new JSONObject().put("type", "json_object"));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("HTTP-Referer", "http://localhost:8000")
                .header("X-Title", "EcoSpot Java")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) return new ArrayList<>();
                    try {
                        JSONObject data = new JSONObject(response.body());
                        String content = data.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                        JSONObject json = new JSONObject(content);
                        JSONArray titlesArray = json.getJSONArray("titles");
                        List<String> titles = new ArrayList<>();
                        for (int i = 0; i < titlesArray.length(); i++) {
                            titles.add(titlesArray.getString(i));
                        }
                        return titles;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new ArrayList<>();
                    }
                });
    }
}
