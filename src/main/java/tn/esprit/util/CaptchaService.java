package tn.esprit.util;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaService {

    private String currentCaptcha;
    private final Random random = new Random();

    public Image generateCaptcha() {
        int width = 150;
        int height = 50;
        
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // Background
        g2d.setColor(new Color(240, 247, 244)); // EcoSpot light green background
        g2d.fillRect(0, 0, width, height);
        
        // Draw some random lines as noise
        g2d.setColor(Color.LIGHT_GRAY);
        for (int i = 0; i < 10; i++) {
            g2d.drawLine(random.nextInt(width), random.nextInt(height), random.nextInt(width), random.nextInt(height));
        }
        
        // Generate random string
        currentCaptcha = generateRandomString(5);
        
        // Render string
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        for (int i = 0; i < currentCaptcha.length(); i++) {
            g2d.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            g2d.drawString(String.valueOf(currentCaptcha.charAt(i)), 20 + i * 25, 35);
        }
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public boolean verify(String input) {
        return currentCaptcha != null && currentCaptcha.equalsIgnoreCase(input);
    }

    public String getCurrentCaptcha() {
        return currentCaptcha;
    }
}
