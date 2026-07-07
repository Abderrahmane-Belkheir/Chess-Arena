package org.Core.UI.Game;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Arc;
import javafx.util.Duration;

public class MatchmakingView {

    private final StackPane root  = new StackPane();
    private final Label msgLabel;
    private final Label     timerLabel;
    private final Runnable  onCancel;

    private Timeline        timerTimeline;
    private int             seconds = 0;
    private int             step    = 0;

    private static final String[][] MESSAGES = {
        {
            "Searching for players near your rating...",
            "Connecting to matchmaking server...",
            "Looking for an opponent..."
        },
        {
            "Expanding search range slightly...",
            "Still looking — won't be long...",
            "Matching by ELO range..."
        },
    };

//    {
//        "Almost there, finding the best match...",
//                "Hang tight — a game is close...",
//                "Finalizing opponent selection..."
//    }

    public MatchmakingView(Runnable onCancel) {
        this.onCancel = onCancel;

        root.setStyle("-fx-background-color: #0a0a0a;");

        GridPane board = buildBoardTexture();

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(480);
        card.setMaxWidth(480);
        card.setPadding(new Insets(24, 32, 24, 32));
        card.setStyle("""
            -fx-background-color: #141414;
            -fx-background-radius: 16;
            -fx-border-color: #222222;
            -fx-border-radius: 16;
            -fx-border-width: 1;
        """);

        StackPane spinner = buildSpinner();

        VBox textBlock = new VBox(6);
        textBlock.setAlignment(Pos.CENTER);

        Label title = new Label("Finding a match");
        title.setStyle("""
            -fx-text-fill: #f0f0f0;
            -fx-font-size: 18px;
            -fx-font-weight: 800;
        """);

        msgLabel = new Label("Searching for players near your rating...");
        msgLabel.setStyle("""
            -fx-text-fill: #555555;
            -fx-font-size: 13px;
            -fx-text-alignment: center;
        """);
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        msgLabel.setMaxWidth(260);

        textBlock.getChildren().addAll(title, msgLabel);

        HBox dots = buildDots();

        timerLabel = new Label("0:00");
        timerLabel.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px; -fx-font-weight: 700;");

        Button cancelBtn = new Button("Cancel search");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #555555;
            -fx-background-radius: 8;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 8;
            -fx-border-width: 1;
            -fx-font-size: 12px;
            -fx-font-weight: 700;
            -fx-padding: 10 0;
            -fx-cursor: hand;
        """);
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
                cancelBtn.getStyle().replace("#2a2a2a", "#444444")
                                    .replace("#555555", "#888888")));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
                cancelBtn.getStyle().replace("#444444", "#2a2a2a")
                                    .replace("#888888", "#555555")));
        cancelBtn.setOnAction(e -> {
            stop();
            onCancel.run(); // TODO: call websocket.stopGameSearching()
        });

        card.getChildren().addAll(spinner, textBlock, dots, timerLabel, cancelBtn);
        root.getChildren().addAll(board, card);

        startAnimations(dots);
    }


    private StackPane buildSpinner() {
        StackPane wrap = new StackPane();
        wrap.setPrefSize(80, 80);


        Region track = new Region();
        track.setPrefSize(80, 80);
        track.setStyle("""
            -fx-background-color: transparent;
            -fx-border-color: #1e1e1e;
            -fx-border-radius: 40;
            -fx-border-width: 3;
        """);

        Arc arc = new Arc(40, 40, 37, 37, 90, 260);
        arc.setType(javafx.scene.shape.ArcType.OPEN);
        arc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        arc.setStroke(javafx.scene.paint.Color.web("#81b64c"));
        arc.setStrokeWidth(3);
        arc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        RotateTransition spin = new RotateTransition(Duration.seconds(1.0), arc);
        spin.setByAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.play();

        Label piece = new Label("♟");
        piece.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 26px;");


        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.4), piece);
        pulse.setFromX(0.85); pulse.setToX(1.0);
        pulse.setFromY(0.85); pulse.setToY(1.0);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        wrap.getChildren().addAll(track, arc, piece);
        return wrap;
    }


    private HBox buildDots() {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER);
        for (int i = 0; i < 3; i++) {
            Region dot = new Region();
            dot.setPrefSize(6, 6);
            dot.setStyle("-fx-background-color: " + (i == 0 ? "#81b64c" : "#2a2a2a")
                    + "; -fx-background-radius: 3;");
            row.getChildren().add(dot);
        }
        return row;
    }

    private void updateDots(HBox dots, int activeStep) {
        for (int i = 0; i < dots.getChildren().size(); i++) {
            Region dot = (Region) dots.getChildren().get(i);
            dot.setStyle("-fx-background-color: " + (i == activeStep ? "#81b64c" : "#2a2a2a")
                    + "; -fx-background-radius: 3;");
        }
    }


    private void startAnimations(HBox dots) {
        java.util.Random rng = new java.util.Random();


        timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            seconds++;
            int m = seconds / 60, s = seconds % 60;
            timerLabel.setText(m + ":" + (s < 10 ? "0" : "") + s);

            if (seconds % 8 == 0) {
                step = Math.min(step + 1, 1);
                String[] pool = MESSAGES[step];
                msgLabel.setText(pool[rng.nextInt(pool.length)]);
                updateDots(dots, step);

                FadeTransition ft = new FadeTransition(Duration.millis(400), msgLabel);
                ft.setFromValue(0.0);
                ft.setToValue(1.0);
                ft.play();
            }
        }));
        timerTimeline.setCycleCount(Animation.INDEFINITE);
        timerTimeline.play();
    }


    private GridPane buildBoardTexture() {
        GridPane grid = new GridPane();
        grid.setOpacity(0.05);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 10; c++) {
                Region sq = new Region();
                sq.setPrefSize(999, 999);
                sq.setStyle("-fx-background-color: "
                        + ((r + c) % 2 == 0 ? "#ffffff" : "#000000") + ";");
                grid.add(sq, c, r);
            }
        }
        return grid;
    }

    // ── Public API ────────────────────────────────────────────────────

    /** Call when match is found before transitioning to game. */
    public void stop() {
        if (timerTimeline != null) timerTimeline.stop();
    }

    public StackPane getView() { return root; }
}