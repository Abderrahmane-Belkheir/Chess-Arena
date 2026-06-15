
package org.Core.Internals.Screens;

import javafx.animation.PauseTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.Getter;

@Getter
public class SplashView {

    private final StackPane view = new StackPane();

    public SplashView(Runnable onFinish) {

        ImageView bg = new ImageView(
                new Image(getClass()
                        .getResource("/chess-background.png")
                        .toExternalForm())
        );

        bg.setFitWidth(1100);
        bg.setFitHeight(700);
        bg.setPreserveRatio(false);

        view.getChildren().add(bg);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> onFinish.run());
        delay.play();
    }

}