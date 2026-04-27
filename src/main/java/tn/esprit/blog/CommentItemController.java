package tn.esprit.blog;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.services.AvatarService;
import java.time.format.DateTimeFormatter;

public class CommentItemController {

    @FXML private Label authorLabel;
    @FXML private Label dateLabel;
    @FXML private Label contentLabel;
    @FXML private ImageView avatarImageView;

    public void setData(Comment comment) {
        authorLabel.setText(comment.getAuthorName());
        contentLabel.setText(comment.getContent());
        
        // Load Avatar
        String seed = comment.getAvatarStyle(); // If null, AvatarService will use the author name as seed
        String avatarUrl = AvatarService.getAvatarUrl(comment.getAuthorName(), seed);
        avatarImageView.setImage(new Image(avatarUrl, true));

        if (comment.getCreatedAt() != null) {
            dateLabel.setText(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        }
    }
}
