package tn.esprit.services;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class WeatherService {
    private static final String API_URL = "https://api.open-meteo.com/v1/forecast";

    public static class ForecastDay {
        private String date;
        private double tempMax;
        private double tempMin;
        private int weatherCode;
        private String description;

        public ForecastDay(String date, double tempMax, double tempMin, int weatherCode, String description) {
            this.date = date;
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.weatherCode = weatherCode;
            this.description = description;
        }

        public String getDate() { return date; }
        public double getTempMax() { return tempMax; }
        public double getTempMin() { return tempMin; }
        public int getWeatherCode() { return weatherCode; }
        public String getDescription() { return description; }
    }

    public List<ForecastDay> getWeeklyForecast(double lat, double lon) {
        List<ForecastDay> forecast = new ArrayList<>();
        try {
            String urlString = API_URL + "?latitude=" + lat + "&longitude=" + lon +
                    "&daily=temperature_2m_max,temperature_2m_min,weather_code&timezone=auto&forecast_days=7";
            
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

                JSONObject data = new JSONObject(response.toString());
                JSONObject daily = data.getJSONObject("daily");
                JSONArray times = daily.getJSONArray("time");
                JSONArray maxTemps = daily.getJSONArray("temperature_2m_max");
                JSONArray minTemps = daily.getJSONArray("temperature_2m_min");
                JSONArray codes = daily.getJSONArray("weather_code");

                for (int i = 0; i < times.length(); i++) {
                    int code = codes.getInt(i);
                    forecast.add(new ForecastDay(
                        times.getString(i),
                        maxTemps.getDouble(i),
                        minTemps.getDouble(i),
                        code,
                        weatherCodeToDescription(code)
                    ));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return forecast;
    }

    private String weatherCodeToDescription(int code) {
        if (code == 0) return "Clear";
        if (code == 1) return "Mainly clear";
        if (code == 2) return "Partly cloudy";
        if (code == 3) return "Overcast";
        if (code >= 45 && code <= 48) return "Fog";
        if (code >= 51 && code <= 57) return "Drizzle";
        if (code >= 61 && code <= 67) return "Rain";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain showers";
        if (code >= 85 && code <= 86) return "Snow showers";
        if (code >= 95) return "Thunderstorm";
        return "Unknown";
    }
}
