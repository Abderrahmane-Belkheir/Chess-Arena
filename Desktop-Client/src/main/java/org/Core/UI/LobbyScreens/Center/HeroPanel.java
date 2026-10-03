package org.Core.UI.LobbyScreens.Center;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

public class HeroPanel {

    private final StackPane root = new StackPane();

    public HeroPanel(LobbyController controller) {

        root.getStyleClass().add("hero-card");

        // ── Chess tile texture (very subtle) ──────────────────────────
        GridPane tiles = buildTileTexture();

        // ── Radial gold glow behind play button ────────────────────────
        StackPane glow = new StackPane();
        glow.setPrefSize(190, 190);
        glow.setMaxSize(190, 190);
        glow.getStyleClass().add("hero-glow");

        // ── Content ───────────────────────────────────────────────────
        VBox content = new VBox(12);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(28, 20, 26, 20));

        // LIVE NOW pill
        HBox livePill = new HBox(6);
        livePill.setAlignment(Pos.CENTER);
        livePill.setPadding(new Insets(4, 12, 4, 12));
        livePill.getStyleClass().add("hero-live-pill");
        livePill.setMaxWidth(Region.USE_PREF_SIZE);

        Region liveDot = new Region();
        liveDot.setPrefSize(8, 8);
        liveDot.getStyleClass().add("hero-live-dot");
        // pulse animation on the dot
        FadeTransition dotPulse = new FadeTransition(Duration.seconds(1.2), liveDot);
        dotPulse.setFromValue(1.0);
        dotPulse.setToValue(0.3);
        dotPulse.setAutoReverse(true);
        dotPulse.setCycleCount(Animation.INDEFINITE);
        dotPulse.play();

        Label liveLabel = new Label("LIVE NOW");
        liveLabel.getStyleClass().add("hero-live-label");
        livePill.getChildren().addAll(liveDot, liveLabel);

        // Title
        Label title = new Label("Make your move");
        title.getStyleClass().add("hero-title");
        title.setWrapText(true);
        title.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Subtitle — two explicit Labels rather than one Label with an embedded
        // "\n": a single Label's preferred-size computation for hard-wrapped
        // text is unreliable (it has clipped or ellipsized the second line
        // depending on wrapText), whereas two one-line Labels always report
        // correct bounds.
        VBox subtitle = new VBox(2);
        subtitle.setAlignment(Pos.CENTER);
        Label subtitleLine1 = new Label("Jump into a match and climb the");
        Label subtitleLine2 = new Label("global leaderboard.");
        subtitleLine1.getStyleClass().add("hero-subtitle");
        subtitleLine2.getStyleClass().add("hero-subtitle");
        subtitle.getChildren().addAll(subtitleLine1, subtitleLine2);

        // ── Play button ───────────────────────────────────────────────
        Button playBtn = new Button("▶   Play Now");
        playBtn.setMaxWidth(260);
        playBtn.setPrefWidth(240);
        playBtn.setPrefHeight(50);
        playBtn.getStyleClass().add("play-button");
        playBtn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), playBtn);
            st.setToX(1.04); st.setToY(1.04); st.play();
        });
        playBtn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), playBtn);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
        // A small tactile "press" dip on top of the hover grow — the button
        // settles back to hover scale (not all the way to 1.0) on release so
        // it doesn't visibly jump if the cursor is still over it.
        playBtn.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(70), playBtn);
            st.setToX(0.97); st.setToY(0.97); st.play();
        });
        playBtn.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), playBtn);
            double target = playBtn.isHover() ? 1.04 : 1.0;
            st.setToX(target); st.setToY(target); st.play();
        });
        playBtn.setOnAction(e -> controller.onPlayClicked());
        // Extra headroom above the button: its permanent glow effect (dropshadow,
        // radius 26) bleeds upward past its layout bounds and would otherwise wash
        // out the subtitle's second line if the two sit right on top of each other.
        VBox.setMargin(playBtn, new Insets(8, 0, 0, 0));

        // ── Stats row ─────────────────────────────────────────────────
        HBox stats = new HBox(0);
        stats.setAlignment(Pos.CENTER);

        Label playing = new Label("👥  12,505 playing");
        playing.getStyleClass().add("hero-stat");

        Label sep = new Label("   |   ");
        sep.getStyleClass().add("hero-stat-sep");

        Label avgWait = new Label("⚡  ~8s avg. match");
        avgWait.getStyleClass().add("hero-stat");

        stats.getChildren().addAll(playing, sep, avgWait);

        content.getChildren().addAll(livePill, title, subtitle, playBtn, stats);

        // Layer: tiles → glow → content
        root.getChildren().addAll(tiles, glow, content);
    }

    // ── helpers ───────────────────────────────────────────────────────

    private GridPane buildTileTexture() {
        // Small squares so the grid's own natural size (rows*size) never
        // exceeds the card's actual content height and forces it taller
        // than intended — the texture should follow the card, not drive it.
        GridPane grid = new GridPane();
        grid.setOpacity(0.045);
        grid.setMouseTransparent(true);
        StackPane.setAlignment(grid, Pos.TOP_LEFT);
        // Sized to stay comfortably under the hero card's own compact width
        // (see LobbyView.SURFACE_WIDTH) — a grid wider than the card would
        // force the card's minWidth past its intended width.
        int tile = 20;
        for (int r = 0; r < 15; r++) {
            for (int c = 0; c < 22; c++) {
                Region sq = new Region();
                sq.setPrefSize(tile, tile);
                sq.getStyleClass().add((r + c) % 2 == 0 ? "hero-tile-light" : "hero-tile-dark");
                grid.add(sq, c, r);
            }
        }
        return grid;
    }

    public StackPane getView() { return root; }
}
