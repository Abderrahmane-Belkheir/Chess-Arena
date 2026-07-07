package org.Core.UI.LobbyScreens.Friends;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;

public final class Avatar {

    private Avatar() {}

    public static StackPane build(String imageUrl, String initials, String bgColor) {
        return build(imageUrl, initials, bgColor, 38);
    }

    public static StackPane build(String imageUrl, String initials, String bgColor, int size) {
        StackPane avatar = new StackPane();
        avatar.setMinSize(size, size);
        avatar.setPrefSize(size, size);
        avatar.setMaxSize(size, size);
        avatar.setClip(new Circle(size / 2.0, size / 2.0, size / 2.0));

        // Build the initials fallback first — always present underneath, so a
        // missing/broken/unreachable image never leaves the avatar blank.
        avatar.setStyle("-fx-background-color: " + bgColor + ";");
        Label lbl = new Label(initials);
        lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: "
                + (size / 3) + "px; -fx-font-weight: 700;");
        avatar.getChildren().add(lbl);


        if (imageUrl != null) {
            try {
                Image image = new Image(imageUrl, size, size, true, true, true);
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(size);
                imageView.setFitHeight(size);
                imageView.setPreserveRatio(true);

                // Background-loaded images fail silently (no exception) — only
                // swap in the real photo once it's confirmed loaded without error.
                image.errorProperty().addListener((obs, wasErr, isErr) -> {
                    if (isErr) avatar.getChildren().remove(imageView);
                });
                image.progressProperty().addListener((obs, oldP, newP) -> {
                    if (newP.doubleValue() >= 1.0 && !image.isError()
                            && !avatar.getChildren().contains(imageView)) {
                        avatar.getChildren().add(imageView);
                        avatar.setStyle(""); // photo now covers the whole circle
                    }
                });
            } catch (Exception ignored) {
                // stays on initials fallback — this is what was previously
                // an uncaught exception for malformed/non-URI paths
            }
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