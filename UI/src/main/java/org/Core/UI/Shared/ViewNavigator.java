package org.Core.UI.Shared;

import com.google.inject.Inject;
import javafx.animation.FadeTransition;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class ViewNavigator {

    private final StackPane root;

    @Inject
    public ViewNavigator(StackPane root) {
        this.root = root;
    }

    public void transitionTo(StackPane newView) {
        javafx.scene.Node old = root.getChildren().isEmpty()
                ? null
                : root.getChildren().get(0);

        if (old == null) {
            root.getChildren().setAll(newView);
            return;
        }

        newView.setOpacity(0);
        root.getChildren().add(newView);

        FadeTransition out = new FadeTransition(Duration.millis(200), old);
        out.setFromValue(1);
        out.setToValue(0);

        FadeTransition in = new FadeTransition(Duration.millis(200), newView);
        in.setFromValue(0);
        in.setToValue(1);

        out.setOnFinished(e -> root.getChildren().remove(old));

        out.play();
        in.play();
    }
}