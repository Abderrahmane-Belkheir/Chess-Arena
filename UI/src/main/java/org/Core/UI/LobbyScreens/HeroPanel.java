package org.Core.UI.LobbyScreens;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * HeroPanel — center section of the lobby.
 *
 * Screenshot breakdown:
 *  - Dark card with rounded corners, subtle border
 *  - Faint chess piece silhouettes scattered as background decoration
 *  - Chessboard tile texture at very low opacity
 *  - Green radial glow emanating from behind the Play button
 *  - "LIVE NOW" pill badge at top-center
 *  - "Make your move" — massive heavy white title
 *  - Subtitle in muted gray
 *  - Large pill-shaped white "▶ Play" button with green glow
 *  - Two small stats below: "12,505 playing | ~8s avg. match"
 */
public class HeroPanel {

    private final StackPane root = new StackPane();

    public HeroPanel(LobbyController controller) {

        root.setStyle("""
            -fx-background-color: #141414;
            -fx-background-radius: 12;
            -fx-border-color: #1e1e1e;
            -fx-border-radius: 12;
            -fx-border-width: 1;
        """);
        root.setMinHeight(500);
        VBox.setVgrow(root, Priority.ALWAYS);

        // ── Chess tile texture (very subtle) ──────────────────────────
        GridPane tiles = buildTileTexture();

        // ── Radial green glow behind play button ──────────────────────
        StackPane glow = new StackPane();
        glow.setPrefSize(420, 420);
        glow.setStyle("""
            -fx-background-color:
                radial-gradient(center 50% 50%, radius 50%,
                    rgba(129,182,76,0.18) 0%,
                    rgba(129,182,76,0.06) 45%,
                    transparent 70%);
        """);

        // ── Content ───────────────────────────────────────────────────
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(48, 40, 48, 40));

        // LIVE NOW pill
        HBox livePill = new HBox(8);
        livePill.setAlignment(Pos.CENTER);
        livePill.setPadding(new Insets(6, 16, 6, 16));
        livePill.setStyle("""
            -fx-background-color: rgba(255,255,255,0.07);
            -fx-background-radius: 20;
            -fx-border-color: rgba(255,255,255,0.12);
            -fx-border-radius: 20;
            -fx-border-width: 1;
        """);
        livePill.setMaxWidth(Region.USE_PREF_SIZE);

        Region liveDot = new Region();
        liveDot.setPrefSize(8, 8);
        liveDot.setStyle("""
            -fx-background-color: #81b64c;
            -fx-background-radius: 4;
        """);
        // pulse animation on the dot
        FadeTransition dotPulse = new FadeTransition(Duration.seconds(1.2), liveDot);
        dotPulse.setFromValue(1.0);
        dotPulse.setToValue(0.3);
        dotPulse.setAutoReverse(true);
        dotPulse.setCycleCount(Animation.INDEFINITE);
        dotPulse.play();

        Label liveLabel = new Label("LIVE NOW");
        liveLabel.setStyle("""
            -fx-text-fill: #cccccc;
            -fx-font-size: 11px;
            -fx-font-weight: 700;
            -fx-letter-spacing: 1.5;
        """);
        livePill.getChildren().addAll(liveDot, liveLabel);

        // Title
        Label title = new Label("Make your move");
        title.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 52px;
            -fx-font-weight: 900;
        """);
        title.setWrapText(true);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Subtitle
        Label subtitle = new Label("Jump into a match and climb the\nglobal leaderboard.");
        subtitle.setStyle("""
            -fx-text-fill: #555555;
            -fx-font-size: 14px;
            -fx-text-alignment: center;
        """);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // ── Play button ───────────────────────────────────────────────
        Button playBtn = new Button("▶   Play");
        playBtn.setPrefWidth(220);
        playBtn.setPrefHeight(58);
        playBtn.setStyle("""
            -fx-background-color: #f5f0e8;
            -fx-text-fill: #0a0a0a;
            -fx-background-radius: 40;
            -fx-font-size: 18px;
            -fx-font-weight: 800;
            -fx-cursor: hand;
        """);
        playBtn.setOnMouseEntered(e -> {
            playBtn.setStyle(playBtn.getStyle().replace("#f5f0e8", "#ffffff"));
            ScaleTransition st = new ScaleTransition(Duration.millis(120), playBtn);
            st.setToX(1.04); st.setToY(1.04); st.play();
        });
        playBtn.setOnMouseExited(e -> {
            playBtn.setStyle(playBtn.getStyle().replace("#ffffff", "#f5f0e8"));
            ScaleTransition st = new ScaleTransition(Duration.millis(120), playBtn);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
        playBtn.setOnAction(e -> controller.onPlayClicked()); // TODO: wire to matchmaking

        // ── Stats row ─────────────────────────────────────────────────
        HBox stats = new HBox(0);
        stats.setAlignment(Pos.CENTER);

        Label playing = new Label("👥  12,505 playing");
        playing.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

        Label sep = new Label("   |   ");
        sep.setStyle("-fx-text-fill: #333333; -fx-font-size: 12px;");

        Label avgWait = new Label("⚡  ~8s avg. match");
        avgWait.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");

        stats.getChildren().addAll(playing, sep, avgWait);

        content.getChildren().addAll(livePill, title, subtitle, playBtn, stats);

        // Layer: tiles → glow → content
        root.getChildren().addAll(tiles, glow, content);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private GridPane buildTileTexture() {
        GridPane grid = new GridPane();
        grid.setOpacity(0.06);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        StackPane.setAlignment(grid, Pos.TOP_LEFT);
        for (int r = 0; r < 9; r++) {
            for (int c = 0; c < 13; c++) {
                Region sq = new Region();
                sq.setPrefSize(80, 80);
                sq.setStyle("-fx-background-color: " +
                        ((r + c) % 2 == 0 ? "#ffffff" : "#000000") + ";");
                grid.add(sq, c, r);
            }
        }
        return grid;
    }

    public StackPane getView() { return root; }
}