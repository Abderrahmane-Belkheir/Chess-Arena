package org.Core.UI.LobbyScreens.Friends;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public final class Avatar {

    private Avatar() {}

    public static StackPane build(String imageUrl, String initials, String bgColor) {
        StackPane avatar = new StackPane();
        avatar.setMinSize(38, 38);
        avatar.setPrefSize(38, 38);

        Circle clip = new Circle(19, 19, 19);
        avatar.setClip(clip);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            Image image = new Image(imageUrl, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(38);
            imageView.setFitHeight(38);
            imageView.setPreserveRatio(true);
            avatar.getChildren().add(imageView);
        } else {
            avatar.setStyle("-fx-background-color: " + bgColor + ";");
            Label lbl = new Label(initials);
            lbl.setStyle("""
                -fx-text-fill: #ffffff;
                -fx-font-size: 13px;
                -fx-font-weight: 700;
            """);
            avatar.getChildren().add(lbl);
        }
        return avatar;
    }

    public static String initials(String username) {
        if (username == null || username.isEmpty()) return "?";
        String[] parts = username.split("_");
        if (parts.length >= 2)
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        return username.substring(0, Math.min(2, username.length())).toUpperCase();
    }

    public static String colorFromName(String username) {
        String[] palette = {
                "#7c5c3e", "#5c3e7c", "#3e7c5c",
                "#7c3e5c", "#3e5c7c", "#5c7c3e"
        };
        return palette[Math.abs(username.hashCode()) % palette.length];
    }
}