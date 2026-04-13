package tn.esprit.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ImageUploadUtils {

    private static final String UPLOAD_DIR = "uploads/";

    public static String saveImage(File sourceFile, String subDir) throws IOException {
        if (sourceFile == null) return null;

        File dir = new File(UPLOAD_DIR + subDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String extension = "";
        String fileName = sourceFile.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i);
        }

        String newFileName = UUID.randomUUID().toString() + extension;
        Path targetPath = Paths.get(UPLOAD_DIR + subDir, newFileName);

        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }

    public static String getImageUrl(String subDir, String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        // Check if it's already a URL
        if (fileName.startsWith("http")) return fileName;
        
        File file = new File(UPLOAD_DIR + subDir + "/" + fileName);
        return file.toURI().toString();
    }
}
