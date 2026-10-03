package org.Core.UI.Game;

import javafx.animation.*;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Arc;
import javafx.util.Duration;

import static org.Core.UI.Shared.LobbySurfaceMetrics.CONTENT_WIDTH;
import static org.Core.UI.Shared.LobbySurfaceMetrics.SURFACE_HEIGHT_FRACTION;
import static org.Core.UI.Shared.LobbySurfaceMetrics.SURFACE_WIDTH;

/**
 * Matchmaking search screen. Shares the lobby's warm-environment + bounded
 * surface treatment — same stylesheet, and critically the exact same surface
 * width/height as LobbyView (LobbySurfaceMetrics), not its own proportions —
 * so clicking Play Now feels like the same application frame staying put
 * with new content inside it, not a differently-sized window popping up.
 */
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

        root.getStylesheets().add(
                getClass().getResource("/css/lobby.css").toExternalForm());
        root.getStyleClass().add("lobby-environment");

        StackPane surface = new StackPane();
        surface.getStyleClass().add("lobby-surface-frame");
        // Exact same metrics as LobbyView's surface (not just "similar"
        // fractions) — otherwise the surface visibly resizes on transition.
        surface.maxWidthProperty().bind(Bindings.min(SURFACE_WIDTH, root.widthProperty().multiply(0.92)));
        surface.maxHeightProperty().bind(root.heightProperty().multiply(SURFACE_HEIGHT_FRACTION));

        GridPane board = buildBoardTexture();
        board.setOpacity(0.04);

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(CONTENT_WIDTH);
        card.setMaxWidth(CONTENT_WIDTH);
        card.setPadding(new Insets(28, 28, 28, 28));
        card.getStyleClass().add("matchmaking-card");

        StackPane spinner = buildSpinner();

        VBox textBlock = new VBox(6);
        textBlock.setAlignment(Pos.CENTER);

        Label title = new Label("Finding a match");
        title.getStyleClass().add("matchmaking-title");

        msgLabel = new Label("Searching for players near your rating...");
        msgLabel.getStyleClass().add("matchmaking-msg");
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        msgLabel.setMaxWidth(260);

        textBlock.getChildren().addAll(title, msgLabel);

        HBox dots = buildDots();

        timerLabel = new Label("0:00");
        timerLabel.getStyleClass().add("matchmaking-timer");

        Button cancelBtn = new Button("Cancel search");
        cancelBtn.setMaxWidth(Double.MAX_VALUE);
        cancelBtn.getStyleClass().add("matchmaking-cancel-btn");
        cancelBtn.setOnAction(e -> {
            stop();
            onCancel.run(); // TODO: call websocket.stopGameSearching()
        });

        card.getChildren().addAll(spinner, textBlock, dots, timerLabel, cancelBtn);
        surface.getChildren().addAll(board, card);
        root.getChildren().add(surface);

        startAnimations(dots);
    }


    private StackPane buildSpinner() {
        StackPane wrap = new StackPane();
        wrap.setPrefSize(80, 80);


        Region track = new Region();
        track.setPrefSize(80, 80);
        track.getStyleClass().add("matchmaking-spinner-track");

        Arc arc = new Arc(40, 40, 37, 37, 90, 260);
        arc.setType(javafx.scene.shape.ArcType.OPEN);
        arc.setFill(javafx.scene.paint.Color.TRANSPARENT);
        arc.setStroke(javafx.scene.paint.Color.web("#c9a961"));
        arc.setStrokeWidth(3);
        arc.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);

        RotateTransition spin = new RotateTransition(Duration.seconds(1.0), arc);
        spin.setByAngle(360);
        spin.setCycleCount(Animation.INDEFINITE);
        spin.setInterpolator(Interpolator.LINEAR);
        spin.play();

        Label piece = new Label("♟");
        piece.getStyleClass().add("matchmaking-piece");


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
            dot.getStyleClass().add(i == 0 ? "matchmaking-dot-active" : "matchmaking-dot-inactive");
            row.getChildren().add(dot);
        }
        return row;
    }

    private void updateDots(HBox dots, int activeStep) {
        for (int i = 0; i < dots.getChildren().size(); i++) {
            Region dot = (Region) dots.getChildren().get(i);
            dot.getStyleClass().setAll(i == activeStep ? "matchmaking-dot-active" : "matchmaking-dot-inactive");
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
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        for (int r = 0; r < 6; r++) {
            for (int c = 0; c < 10; c++) {
                Region sq = new Region();
                sq.setPrefSize(999, 999);
                sq.getStyleClass().add((r + c) % 2 == 0 ? "matchmaking-tile-light" : "matchmaking-tile-dark");
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
