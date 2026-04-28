package tn.esprit.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExportService {

    public static void exportToCSV(List<Map<String, Object>> data, File file) {
        if (data == null || data.isEmpty()) return;

        List<String> keys = new ArrayList<>(data.get(0).keySet());

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            // Header
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) line.append(",");
                line.append(escape(keys.get(i)));
            }
            writer.write(line + "\n");

            // Rows — iterate by same key order as header
            for (Map<String, Object> row : data) {
                line.setLength(0);
                for (int i = 0; i < keys.size(); i++) {
                    if (i > 0) line.append(",");
                    line.append(escape(String.valueOf(row.get(keys.get(i)))));
                }
                writer.write(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // RFC 4180: wrap in quotes if value contains comma, newline, or double-quote; escape internal quotes by doubling
    private static String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
