package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.util.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class OpenRouterService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = Config.get("OPENROUTER_API_KEY");

    public static class AiResponse {
        public List<String> tasks = new ArrayList<>();
        public String suggestedPriority = "MEDIUM";
    }

    public static class DetectionResult {
        public boolean isSpam;
        public String reason;
    }

    public AiResponse generateTasks(String title, String description) {
        AiResponse result = new AiResponse();
        if (API_KEY == null || API_KEY.isEmpty()) {
            System.err.println("OpenRouter API key is missing.");
            return result;
        }

        String prompt = "You are an environmental community organizer. Based on the ticket title and description below:\n" +
                "1. Generate a list of 3-7 short, actionable, and practical tasks (instructions).\n" +
                "2. Suggest an overall priority for the ticket (LOW, MEDIUM, HIGH, or URGENT).\n\n" +
                "Ticket Title: " + title + "\n" +
                "Ticket Description: " + description + "\n\n" +
                "Reply ONLY with a JSON object containing:\n" +
                "- \"tasks\": An array of strings (the tasks).\n" +
                "- \"suggested_priority\": A string (LOW, MEDIUM, HIGH, or URGENT).";

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HTTP-Referer", "http://localhost:8000"); // Optional for OpenRouter
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("model", "openrouter/auto");
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "You are an assistant that outputs ONLY a JSON object with the requested keys."));
            messages.put(new JSONObject().put("role", "user").put("content", prompt));
            
            payload.put("messages", messages);
            payload.put("response_format", new JSONObject().put("type", "json_object"));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                
                JSONObject decoded = new JSONObject(content);
                JSONArray tasksArr = decoded.getJSONArray("tasks");
                for (int i = 0; i < tasksArr.length(); i++) {
                    result.tasks.add(tasksArr.getString(i));
                }
                result.suggestedPriority = decoded.optString("suggested_priority", "MEDIUM");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public DetectionResult checkSpam(String title, String description) {
        DetectionResult result = new DetectionResult();
        result.isSpam = false;
        result.reason = "";

        if (API_KEY == null || API_KEY.isEmpty()) {
            return result;
        }

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HTTP-Referer", "http://localhost:8000");
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            payload.put("model", "openrouter/auto");
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "You are a precise spam filter. Output ONLY valid JSON."));
            messages.put(new JSONObject().put("role", "user").put("content", 
                "Analyze if this ticket is spam (gibberish or unrelated to environment/nature).\n" +
                "Title: " + title + "\nDescription: " + description + "\n\n" +
                "Reply ONLY with a JSON object: {\"is_spam\": boolean, \"reason\": \"string\"}"));
            
            payload.put("messages", messages);
            payload.put("response_format", new JSONObject().put("type", "json_object"));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = payload.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder res = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) res.append(line);
                in.close();

                JSONObject data = new JSONObject(res.toString());
                String content = data.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                JSONObject decoded = new JSONObject(content);
                result.isSpam = decoded.getBoolean("is_spam");
                result.reason = decoded.optString("reason", "");
            } else {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder err = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) err.append(line);
                in.close();
                System.err.println("Spam check failed (" + conn.getResponseCode() + "): " + err.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
