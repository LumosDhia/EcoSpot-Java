package tn.esprit.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExportService {

    public static void exportToCSV(List<Map<String, Object>> data, File file) {
        if (data == null || data.isEmpty()) return;

        try (FileWriter writer = new FileWriter(file)) {
            // Write Header
            Map<String, Object> firstRow = data.get(0);
            boolean first = true;
            for (String key : firstRow.keySet()) {
                if (!first) writer.append(",");
                writer.append(key);
                first = false;
            }
            writer.append("\n");

            // Write Data
            for (Map<String, Object> row : data) {
                first = true;
                for (Object value : row.values()) {
                    if (!first) writer.append(",");
                    writer.append(String.valueOf(value));
                    first = false;
                }
                writer.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
