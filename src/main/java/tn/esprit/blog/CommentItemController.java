package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.format.DateTimeFormatter;

public class CommentItemController {
    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label contentLabel;

    public void setData(Comment comment) {
        authorLabel.setText(comment.getAuthor());
        contentLabel.setText(comment.getContent());
        
        if (comment.getCreatedAt() != null) {
            dateLabel.setText(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }
}
