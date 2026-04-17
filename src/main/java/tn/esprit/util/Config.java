package tn.esprit.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Config {
    private static final Map<String, String> env = new HashMap<>();

    static {
        load();
    }

    private static void load() {
        String envPath = Paths.get("").toAbsolutePath().toString() + "/.env";
        try (BufferedReader reader = new BufferedReader(new FileReader(envPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    env.put(parts[0].trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load .env file: " + e.getMessage());
        }
    }

    public static String get(String key) {
        return env.get(key);
    }

    public static String get(String key, String defaultValue) {
        return env.getOrDefault(key, defaultValue);
    }
}
