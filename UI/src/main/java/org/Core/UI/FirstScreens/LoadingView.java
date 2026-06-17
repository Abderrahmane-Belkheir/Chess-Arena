package org.Core.UI.FirstScreens;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * LoadingView — shown while exchanging auth code for access token
 * and fetching user profile.
 *
 * Stages:
 *   1. "Signing you in..."        ← token exchange
 *   2. "Loading your profile..."  ← fetching user data
 *   3. "Almost there..."          ← final setup
 *
 * Usage:
 *   LoadingView loading = new LoadingView();
 *   transitionTo(loading.getView());
 *
 *   // update message as steps complete
 *   loading.setMessage("Loading your profile...");
 *
 *   // when done → transitionTo(lobbyController.buildView())
 */
public class LoadingView {

    private final StackPane root = new StackPane();
    private final Label messageLabel = new Label("Signing you in...");
    private final Label subLabel     = new Label("Securely exchanging credentials");

    public LoadingView() {

        root.setStyle("-fx-background-color: #0a0a0a;");
        root.setMinSize(600, 400);

        VBox content = new VBox(28);
        content.setAlignment(Pos.CENTER);

        // ── Spinner ───────────────────────────────────────────────────
        StackPane spinnerWrap = new StackPane();
        spinnerWrap.setPrefSize(72, 72);

        // background circle (track)
        Circle track = new Circle(34);
        track.setFill(Color.TRANSPARENT);
        track.setStroke(Color.web("#1e1e1e"));
        track.setStrokeWidth(3);

        // spinning arc
        Arc arc = new Arc(0, 0, 34, 34, 90, 260);
        arc.setType(ArcType.OPEN);
        arc.setFill(Color.TRANSPARENT);
        arc.setStroke(Color.web("#81b64c"));
        arc.setStrokeWidth(3);
        arc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        // chess piece in the center
        Label piece = new Label("♟");
        piece.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 22px;");

        // pulse the piece
        ScaleTransition piecePulse = new ScaleTransition(Duration.seconds(1.4), piece);
        piecePulse.setFromX(0.85); piecePulse.setToX(1.0);
        piecePulse.setFromY(0.85); piecePulse.setToY(1.0);
        piecePulse.setAutoReverse(true);
        piecePulse.setCycleCount(Animation.INDEFINITE);
        piecePulse.play();

        // rotate the arc
        RotateTransition spin = new RotateTransition(Duration.seconds(1.0), arc);
        spin.setByAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.play();

        spinnerWrap.getChildren().addAll(track, arc, piece);

        // ── Text ──────────────────────────────────────────────────────
        VBox textBlock = new VBox(8);
        textBlock.setAlignment(Pos.CENTER);

        messageLabel.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 18px;
            -fx-font-weight: 800;
        """);

        subLabel.setStyle("""
            -fx-text-fill: #444444;
            -fx-font-size: 13px;
        """);

        textBlock.getChildren().addAll(messageLabel, subLabel);

        // ── Step dots ─────────────────────────────────────────────────
        HBox dots = new HBox(8);
        dots.setAlignment(Pos.CENTER);

        for (int i = 0; i < 3; i++) {
            Circle dot = new Circle(4);
            dot.setFill(Color.web(i == 0 ? "#81b64c" : "#2a2a2a"));
            dots.getChildren().add(dot);
        }

        content.getChildren().addAll(spinnerWrap, textBlock, dots);
        root.getChildren().add(content);
    }

    // ── public API ────────────────────────────────────────────────────

    /**
     * Update the main message and highlight the matching step dot.
     * Call from Platform.runLater().
     *
     * @param message  short status string shown to the user
     * @param step     0 = token exchange, 1 = profile fetch, 2 = almost there
     */
    public void setMessage(String message, int step) {
        messageLabel.setText(message);

        // update dot highlights
        HBox dots = (HBox) ((VBox) root.getChildren().get(0)).getChildren().get(2);
        for (int i = 0; i < dots.getChildren().size(); i++) {
            Circle dot = (Circle) dots.getChildren().get(i);
            dot.setFill(Color.web(i == step ? "#81b64c" : "#2a2a2a"));
        }

        // update subtitle per step
        switch (step) {
            case 0 -> subLabel.setText("Securely exchanging credentials");
            case 1 -> subLabel.setText("Fetching your profile and rating");
            case 2 -> subLabel.setText("Setting up your lobby...");
        }
    }

    public StackPane getView() { return root; }
}