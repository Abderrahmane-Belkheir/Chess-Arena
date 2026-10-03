package org.Core.UI.LobbyScreens.Friends;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Button;
import javafx.util.Duration;

/**
 * App-wide gate for the Invite/Watch actions — only one can be committed at
 * a time, from any friend row/card. install() leaves btn exactly where it
 * is (no wrapper node) — clicking it (while the gate is open) closes the
 * gate, greys the button out, fires the action, and fills the grey back to
 * the button's real color left-to-right over the cooldown window, then
 * restores its normal styling and reopens the gate.
 */
public final class ActionThrottle {

    private static final Duration COOLDOWN = Duration.seconds(6);
    private static final String TRACK_COLOR = "#4a453c";
    private static final String FILL_COLOR  = "#e8cf8a";
    private static boolean locked = false;

    private ActionThrottle() {}

    /**
     * Wires btn's click to run action through the global gate. A click
     * while the gate is already closed (another invite/watch in flight)
     * does nothing.
     */
    public static void install(Button btn, Runnable action) {
        String restStyle = btn.getStyle();

        DoubleProperty progress = new SimpleDoubleProperty(0);
        progress.addListener((obs, old, val) -> {
            double pct = val.doubleValue() * 100;
            btn.setStyle(restStyle + " -fx-background-color: linear-gradient(to right, "
                    + FILL_COLOR + " " + pct + "%, " + TRACK_COLOR + " " + pct + "%);");
        });

        btn.setOnAction(e -> {
            if (locked) return;
            locked = true;
            btn.setDisable(true);
            btn.setStyle(restStyle + " -fx-background-color: " + TRACK_COLOR + ";"); // instant grey, no flash of the old color

            Timeline fill = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progress, 0, Interpolator.LINEAR)),
                    new KeyFrame(COOLDOWN, new KeyValue(progress, 1, Interpolator.LINEAR))
            );
            fill.setOnFinished(ev -> {
                btn.setStyle(restStyle);
                btn.setDisable(false);
                locked = false;
            });
            fill.play();

            action.run();
        });
    }
}
