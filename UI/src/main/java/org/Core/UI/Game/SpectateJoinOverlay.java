package org.Core.UI.Game;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.shape.*;
import javafx.util.Duration;

/**
 * Small "casting a hook" intro shown while joining a game as a spectator.
 * A curved line draws itself in, "catches" with a pulsing dot, then a label
 * fades up — purely decorative, meant to cover the brief gap before the
 * GameView finishes constructing/rendering.
 *
 * Usage:
 *   SpectateJoinOverlay overlay = new SpectateJoinOverlay(spectatedUsername);
 *   viewNavigator.transitionTo(overlay.getView());
 *   // ...build GameView...
 *   overlay.playThenRun(() -> viewNavigator.transitionTo(gameView.getView()));
 */
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.control.Label;
import javafx.scene.shape.*;
import javafx.util.Duration;


public class SpectateJoinOverlay {

    private static final String ACCENT      = "#e8cf8a";
    private static final String ACCENT_GLOW = "rgba(232,207,138,0.6)";
    private static final String TEXT_MUTED  = "#a08e78";
    private static final String BG_TOP      = "#2a2018";
    private static final String BG_BOTTOM   = "#180f0a";

    private static final double APPROX_LINE_LENGTH = 190;

    private final StackPane root = new StackPane();
    private final Path      hookLine;
    private final Circle    catchDot;
    private final Label     label;

    public SpectateJoinOverlay(String spectatedUsername) {
        root.setStyle("-fx-background-color: linear-gradient(to bottom, " + BG_TOP + ", " + BG_BOTTOM + ");");

        hookLine = new Path();
        hookLine.setStroke(Color.web(ACCENT));
        hookLine.setStrokeWidth(2.5);
        hookLine.setFill(Color.TRANSPARENT);
        hookLine.setStrokeLineCap(StrokeLineCap.ROUND);
        hookLine.getElements().addAll(
                new MoveTo(0, -90),
                new CubicCurveTo(6, -30, -18, 10, 0, 55)
        );
        hookLine.getStrokeDashArray().setAll(APPROX_LINE_LENGTH);
        hookLine.setStrokeDashOffset(APPROX_LINE_LENGTH);

        catchDot = new Circle(6, Color.web(ACCENT));
        catchDot.setTranslateY(55);
        catchDot.setOpacity(0);
        catchDot.setEffect(new DropShadow(12, Color.web(ACCENT_GLOW)));
        // Cache the glow so it's rasterized once and just alpha-blended during
        // opacity animation, instead of being recomputed every frame — without
        // this, the DropShadow can visibly flash/brighten right as the fade starts.
        catchDot.setCache(true);
        catchDot.setCacheHint(CacheHint.SPEED);

        label = new Label("Hooking into " + spectatedUsername + "'s game…");
        label.setStyle("-fx-text-fill: " + TEXT_MUTED + "; -fx-font-size: 13px; -fx-font-weight: 600;");
        label.setOpacity(0);
        label.setTranslateY(90);

        StackPane hookGroup = new StackPane(hookLine, catchDot);
        VBox box = new VBox(24, hookGroup, label);
        box.setAlignment(Pos.CENTER);

        root.getChildren().add(box);
        // Cache the whole overlay too — root's opacity is what actually animates
        // during fadeOut, so its subtree (including the cached glow bitmap above)
        // should be composited as a flat layer rather than re-rendered per frame.
        root.setCache(true);
        root.setCacheHint(CacheHint.SPEED);
    }

    public StackPane getView() {
        return root;
    }

    /** Runs the cast/catch/reveal animation, holds briefly, then fades the overlay out and calls onFinished. */
    public void playThenRun(Runnable onFinished) {
        SequentialTransition intro = buildIntroAnimation();
        PauseTransition hold = new PauseTransition(Duration.millis(500));
        FadeTransition fadeOut = new FadeTransition(Duration.millis(250), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (onFinished != null) onFinished.run();
        });

        SequentialTransition full = new SequentialTransition(intro, hold, fadeOut);
        full.play();
    }

    private SequentialTransition buildIntroAnimation() {
        // Draw the line in
        Timeline drawLine = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(hookLine.strokeDashOffsetProperty(), APPROX_LINE_LENGTH)),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(hookLine.strokeDashOffsetProperty(), 0, Interpolator.EASE_BOTH))
        );

        // "Catch" — dot pops in with a little overshoot
        FadeTransition dotFade = new FadeTransition(Duration.millis(150), catchDot);
        dotFade.setToValue(1.0);
        ScaleTransition dotPop = new ScaleTransition(Duration.millis(350), catchDot);
        dotPop.setFromX(0.3); dotPop.setFromY(0.3);
        dotPop.setToX(1.15);  dotPop.setToY(1.15);
        dotPop.setInterpolator(Interpolator.EASE_OUT);
        ScaleTransition dotSettle = new ScaleTransition(Duration.millis(150), catchDot);
        dotSettle.setToX(1.0);
        dotSettle.setToY(1.0);

        ParallelTransition catchMoment = new ParallelTransition(dotFade, dotPop);

        // Label fades up after the catch
        FadeTransition labelFade = new FadeTransition(Duration.millis(300), label);
        labelFade.setToValue(1.0);
        TranslateTransition labelSlide = new TranslateTransition(Duration.millis(300), label);
        labelSlide.setToY(0);
        ParallelTransition revealLabel = new ParallelTransition(labelFade, labelSlide);

        return new SequentialTransition(drawLine, catchMoment, dotSettle, revealLabel);
    }
}