package tn.esprit.services;

import tn.esprit.util.MyConnection;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Random;

public class AvatarService {

    private static final String BASE_URL = "https://api.dicebear.com/9.x/";
    private static final String FIXED_STYLE = "pixel-art";
    
    // Predefined seeds for a "gallery" of choices
    private static final String[] PRESET_SEEDS = {
        "Felix", "Aneka", "Jocelyn", "Jasper", "Jude", "Adrian", "Lulu", "Vivian", 
        "Bibi", "Caleb", "Destiny", "George", "Milo", "Kiki", "Sasha", "Oreo"
    };

    /**
     * Returns the fixed style used for the whole website.
     */
    public static String getWebsiteStyle() {
        return FIXED_STYLE;
    }

    /**
     * Gets an avatar URL. If a seed is provided, it uses it. 
     * If no seed is provided, it defaults to the username.
     */
    public static String getAvatarUrl(String username, String customSeed) {
        String seed = (customSeed != null && !customSeed.isBlank()) ? customSeed : username;
        try {
            if (seed == null || seed.isEmpty()) seed = "default";
            String encodedSeed = URLEncoder.encode(seed, StandardCharsets.UTF_8.toString());
            return String.format("%s%s/png?seed=%s", BASE_URL, FIXED_STYLE, encodedSeed);
        } catch (UnsupportedEncodingException e) {
            return String.format("%s%s/png?seed=default", BASE_URL, FIXED_STYLE);
        }
    }

    public static String[] getPresetSeeds() {
        return PRESET_SEEDS;
    }

    public static String getRandomStyle() {
        return PRESET_SEEDS[new Random().nextInt(PRESET_SEEDS.length)];
    }
}
