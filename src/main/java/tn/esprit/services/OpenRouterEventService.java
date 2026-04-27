package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import tn.esprit.event.Event;
import tn.esprit.util.Config;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Specialized AI Service for the Event Module.
 */
public class OpenRouterEventService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = Config.get("OPENROUTER_API_KEY");

    public static class PredictionResult {
        public String successLevel; // e.g., "HIGH", "MEDIUM", "LOW"
        public String analysis;     // The reasoning/explanation
    }

    /**
     * Predicts the attendance success of an event using AI analysis.
     */
    public PredictionResult predictAttendance(Event event) {
        PredictionResult result = new PredictionResult();
        result.successLevel = "UNKNOWN";
        result.analysis = "AI Analysis unavailable.";

        if (API_KEY == null || API_KEY.isEmpty()) {
            result.analysis = "OpenRouter API key is missing in .env.";
            return result;
        }

        String prompt = String.format(
            "You are an expert environmental event consultant. Analyze this event and predict its attendance success:\n\n" +
            "Event Name: %s\n" +
            "Location: %s\n" +
            "Description: %s\n" +
            "Planned Capacity: %d\n" +
            "Date: %s\n\n" +
            "Consider factors like: Is the location popular? Is the name catchy? Does the description sound engaging? " +
            "Is the date on a weekend (higher success) or weekday?\n\n" +
            "Reply ONLY with a JSON object containing:\n" +
            "- \"success_level\": A string (HIGH, MEDIUM, or LOW).\n" +
            "- \"analysis\": A 2-sentence professional explanation of your prediction.",
            event.getName(), event.getLocation(), event.getDescription(), 
            event.getCapacity(), event.getStartedAt().toString()
        );

        try {
            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Authorization", "Bearer " + API_KEY);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("HTTP-Referer", "http://localhost:8080");
            conn.setConnectTimeout(10000); // 10 seconds
            conn.setReadTimeout(30000);    // 30 seconds
            conn.setDoOutput(true);

            JSONObject payload = new JSONObject();
            // Use openrouter/auto to find the best available model automatically
            payload.put("model", "openrouter/auto"); 
            
            JSONArray messages = new JSONArray();
            messages.put(new JSONObject().put("role", "system").put("content", "You output ONLY valid JSON."));
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
                while ((inputLine = in.readLine()) != null) response.append(inputLine);
                in.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                String content = jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
                
                JSONObject decoded = new JSONObject(content);
                result.successLevel = decoded.optString("success_level", "MEDIUM");
                result.analysis = decoded.optString("analysis", "Prediction complete.");
            } else {
                BufferedReader err = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errRes = new StringBuilder();
                String line;
                while ((line = err.readLine()) != null) errRes.append(line);
                err.close();
                System.err.println("AI Error (" + conn.getResponseCode() + "): " + errRes.toString());
                result.analysis = "Error from AI Service: " + conn.getResponseCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.analysis = "Internal error during AI analysis: " + e.getMessage();
        }
        return result;
    }
}
