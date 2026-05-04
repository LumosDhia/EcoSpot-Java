package tn.esprit.util;

import tn.esprit.blog.Blog;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class RSSGenerator {

    private static final DateTimeFormatter RSS_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    public static String generateRSS(List<Blog> blogs, String categoryName) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
        sb.append("<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n");
        sb.append("<channel>\n");
        
        sb.append("  <title>EcoSpot Blog - ").append(categoryName != null ? categoryName : "All").append("</title>\n");
        sb.append("  <link>http://localhost:8000/blog</link>\n");
        sb.append("  <description>Latest environmental insights and stories from EcoSpot</description>\n");
        sb.append("  <language>en-us</language>\n");
        sb.append("  <lastBuildDate>").append(java.time.ZonedDateTime.now().format(RSS_DATE_FORMAT)).append("</lastBuildDate>\n");

        for (Blog b : blogs) {
            sb.append("  <item>\n");
            sb.append("    <title><![CDATA[").append(b.getTitle()).append("]]></title>\n");
            sb.append("    <link>http://localhost:8000/blog/").append(b.getId()).append("</link>\n");
            sb.append("    <guid isPermaLink=\"false\">ecospot-blog-").append(b.getId()).append("</guid>\n");
            sb.append("    <description><![CDATA[").append(truncateContent(b.getContent(), 200)).append("]]></description>\n");
            
            if (b.getPublishedAt() != null) {
                String pubDate = b.getPublishedAt().atZone(ZoneId.systemDefault()).format(RSS_DATE_FORMAT);
                sb.append("    <pubDate>").append(pubDate).append("</pubDate>\n");
            }
            
            if (b.getCategory() != null) {
                sb.append("    <category>").append(b.getCategory().getName()).append("</category>\n");
            }
            
            sb.append("  </item>\n");
        }

        sb.append("</channel>\n");
        sb.append("</rss>");
        return sb.toString();
    }

    private static String truncateContent(String content, int max) {
        if (content == null) return "";
        String plain = content.replaceAll("<[^>]*>", ""); // Simple HTML strip
        if (plain.length() <= max) return plain;
        return plain.substring(0, max) + "...";
    }
}
