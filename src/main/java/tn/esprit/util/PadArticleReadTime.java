package tn.esprit.util;

import java.sql.*;
import java.util.Random;

/**
 * One-shot utility: appends random lorem-ipsum paragraphs to every article so
 * that each ends up at least 1 minute longer than it currently is, with enough
 * variation across articles to land at different total reading times (>3, >4, >5 …).
 *
 * Reading-time formula (matches Blog.getReadingTime): ceil(wordCount / 200).
 * Run once; safe to re-run (it only appends, and the words added are idempotent
 * enough that the result will just grow slightly on a second run).
 */
public class PadArticleReadTime {

    private static final int WPM = 200;

    // 20 lorem-ipsum sentences (~12-18 words each) to pick from randomly
    private static final String[] SENTENCES = {
        "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore.",
        "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo.",
        "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.",
        "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est.",
        "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium totam rem.",
        "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit sed quia consequuntur magni.",
        "Neque porro quisquam est qui dolorem ipsum quia dolor sit amet consectetur adipisci velit sed numquam.",
        "Ut labore et dolore magnam aliquam quaerat voluptatem animi beatae vitae dicta sunt explicabo nemo ipsa.",
        "Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur.",
        "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti.",
        "Nam libero tempore cum soluta nobis eligendi optio cumque nihil impedit quo minus id quod maxime.",
        "Temporibus autem quibusdam et aut officiis debitis rerum necessitatibus saepe eveniet ut et voluptates.",
        "Itaque earum rerum hic tenetur a sapiente delectus ut aut reiciendis voluptatibus maiores alias consequatur.",
        "Similique sunt in culpa qui officia deserunt mollitia animi id est laborum et dolorum fuga harum.",
        "Quis custodiet ipsos custodes et rerum novarum studio flagrant atque novae res quaerunt nec poenam recusant.",
        "Cum ceteris in veneratione tui montes inaccessos colitur maxima etiam pro nobis facienda et laudanda sunt.",
        "Praesent commodo cursus magna vel scelerisque nisl consectetur et nullam quis risus eget urna mollis ornare.",
        "Donec sed odio dui cras justo odio dapibus ac facilisis in egestas eget quam nullam quis.",
        "Fusce dapibus tellus ac cursus commodo tortor mauris condimentum nibh ut fermentum massa justo sit amet.",
        "Nullam id dolor id nibh ultricies vehicula ut id elit aenean lacinia bibendum nulla sed consectetur.",
    };

    public static void main(String[] args) {
        Random rng = new Random();
        try {
            Connection cnx = MyConnection.getInstance().getCnx();

            // Fetch all articles
            ResultSet rs = cnx.createStatement().executeQuery("SELECT id, content FROM article");
            PreparedStatement ps = cnx.prepareStatement("UPDATE article SET content = ? WHERE id = ?");

            int updated = 0;
            while (rs.next()) {
                int id = rs.getInt("id");
                String content = rs.getString("content");
                if (content == null) content = "";

                // Current word count (matches Blog.getReadingTime logic)
                int currentWords = content.split("\\s+").length;
                int currentMinutes = (int) Math.ceil(currentWords / (double) WPM);

                // Random extra: 1 to 3 additional minutes
                int extraMinutes = 1 + rng.nextInt(3);
                int targetMinutes = currentMinutes + extraMinutes;
                int targetWords   = targetMinutes * WPM;
                int wordsNeeded   = Math.max(0, targetWords - currentWords);

                if (wordsNeeded == 0) {
                    System.out.println("Article " + id + ": already " + currentMinutes + "min, skipping.");
                    continue;
                }

                String padding = buildPadding(wordsNeeded, rng);
                String newContent = content + "\n" + padding;

                ps.setString(1, newContent);
                ps.setInt(2, id);
                ps.executeUpdate();

                int newMinutes = (int) Math.ceil(newContent.split("\\s+").length / (double) WPM);
                System.out.println("Article " + id + ": " + currentMinutes + "min → " + newMinutes + "min (+"+extraMinutes+"min target, added ~"+wordsNeeded+" words)");
                updated++;
            }

            System.out.println("\nDone. Updated " + updated + " articles.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Builds lorem-ipsum HTML paragraphs totalling at least wordsNeeded words. */
    private static String buildPadding(int wordsNeeded, Random rng) {
        StringBuilder html = new StringBuilder();
        int wordCount = 0;
        while (wordCount < wordsNeeded) {
            // Pick 3-5 sentences per paragraph
            int sentCount = 3 + rng.nextInt(3);
            StringBuilder para = new StringBuilder("<p>");
            for (int i = 0; i < sentCount; i++) {
                String sentence = SENTENCES[rng.nextInt(SENTENCES.length)];
                para.append(sentence).append(" ");
                wordCount += sentence.split("\\s+").length;
            }
            para.append("</p>");
            html.append(para);
        }
        return html.toString();
    }
}
