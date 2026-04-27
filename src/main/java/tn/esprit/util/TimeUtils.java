package tn.esprit.util;

import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

public class TimeUtils {

    public static String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(dateTime, now);
        
        long seconds = duration.getSeconds();
        if (seconds < 0) return "Just now"; // Future date?

        if (seconds < 60) {
            return "Just now";
        }

        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + " minute" + (minutes > 1 ? "s" : "") + " ago";
        }

        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        }

        long days = duration.toDays();
        if (days < 30) {
            return days + " day" + (days > 1 ? "s" : "") + " ago";
        }

        // More than 30 days, return formatted date
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
