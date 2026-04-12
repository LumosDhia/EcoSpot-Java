package tn.esprit.blog;

public final class ArticleInputValidator {

    private ArticleInputValidator() {}

    public static String validate(String title, String htmlContent, boolean hasSelectedCategory) {
        if (title == null || title.trim().isEmpty()) {
            return "Title is required.";
        }
        if (title.length() < 5 || title.length() > 100) {
            return "Title must be between 5 and 100 characters.";
        }
        if (!title.matches(".*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*")) {
            return "Title must contain at least 5 letters.";
        }
        if (!title.substring(0, 1).matches("[a-zA-Z]")) {
            return "Title must start with a letter.";
        }

        String content = htmlContent == null ? "" : htmlContent;
        String plainText = content.replaceAll("<[^>]*>", "").trim();
        if (plainText.isEmpty()) {
            return "Content is required.";
        }
        if (plainText.length() < 20) {
            return "The article content must be more detailed (at least 20 characters).";
        }
        if (!plainText.matches(".*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*[a-zA-Z].*")) {
            return "The article content must contain at least 5 letters.";
        }

        if (!hasSelectedCategory) {
            return "Please select a category.";
        }

        return null;
    }
}
