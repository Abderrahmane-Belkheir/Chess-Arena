package org.Core.UI.Game;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.CacheHint;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.Core.Auth.DTO.UserSession;
import org.Core.Game.Events.GameOverInfo;
import org.Core.UI.LobbyScreens.Friends.Avatar;
public class GameOverCard {

    // ── Palette (matches GameView) ────────────────────────────────────
    private static final String BG_OVERLAY       = "rgba(20,18,16,0.55)";
    private static final String CARD_BG          = "#262421";
    private static final String CARD_BORDER      = "#3a3836";
    private static final String WIN_COLOR         = "#81b64c";
    private static final String LOSE_COLOR        = "#e05555";
    private static final String DRAW_COLOR        = "#a0a090";
    private static final String TEXT_PRIMARY      = "#e8e4dc";
    private static final String TEXT_SECONDARY    = "#7a7672";
    private static final String ROW_BG            = "#1e1c1a";
    private static final String BTN_PRIMARY_BG    = "#81b64c";
    private static final String BTN_SECONDARY_BG  = "#3a3836";
    private static final double BLUR_RADIUS = 10;
    private static final double BLUR_OVERSCAN = BLUR_RADIUS * 2;
    private static final double CARD_SIZE = 300;

    private final StackPane boardWrap;
    private final StackPane overlay = new StackPane();
    private final GaussianBlur blurEffect = new GaussianBlur(0);
    private ImageView snapshotBlur;


    /** @param boardWrap the StackPane that hosts the board — the card and blur snapshot are added as its children. */
    public GameOverCard(GameOverInfo info, UserSession session, StackPane boardWrap,
                        Runnable onNewGame, Runnable onReturnToLobby) {
        this.boardWrap = boardWrap;

        overlay.setStyle("-fx-background-color: " + BG_OVERLAY + ";");
        overlay.setOpacity(0);
        overlay.setPickOnBounds(true);

        VBox card = buildCard(info, session, onNewGame, onReturnToLobby);
        card.setCache(true);
        card.setCacheHint(CacheHint.SPEED);

        applyGlow(card, resultGlowColor(info.getResult()));

        card.setScaleX(0.9);
        card.setScaleY(0.9);
        card.setOpacity(0);

        overlay.getChildren().add(card);
        StackPane.setAlignment(card, Pos.CENTER);

        addBlurSnapshot();
        boardWrap.getChildren().add(overlay);

        playEntranceAnimation(card);
    }

    public StackPane getView() { return overlay; }

    public void dispose(Runnable onFinished) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(160), overlay);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> {
            boardWrap.getChildren().remove(overlay);
            if (snapshotBlur != null) boardWrap.getChildren().remove(snapshotBlur);
            if (onFinished != null) onFinished.run();
        });
        fadeOut.play();
    }


    private void addBlurSnapshot() {
        // Clip boardWrap to its own bounds so any overscan on the blurred image gets hidden
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(boardWrap.widthProperty());
        clip.heightProperty().bind(boardWrap.heightProperty());
        boardWrap.setClip(clip);

        SnapshotParameters params = new SnapshotParameters();
        // Use the actual dark background color instead of transparent —
        // this prevents the blurred edge from fading toward transparency/black
        params.setFill(Color.web("#262421"));
        Image snapshot = boardWrap.snapshot(params, null);

        snapshotBlur = new ImageView(snapshot);
        snapshotBlur.setMouseTransparent(true);
        snapshotBlur.setCache(true);
        snapshotBlur.setCacheHint(CacheHint.SPEED);
        snapshotBlur.setPreserveRatio(false);

        // Oversize beyond boardWrap's bounds so blur has real pixels past the visible edge
        snapshotBlur.fitWidthProperty().bind(boardWrap.widthProperty().add(BLUR_OVERSCAN * 2));
        snapshotBlur.fitHeightProperty().bind(boardWrap.heightProperty().add(BLUR_OVERSCAN * 2));

        // Center the oversized image so the extra margin is distributed evenly on all sides
        snapshotBlur.setTranslateX(0);
        snapshotBlur.setTranslateY(0);

        snapshotBlur.setEffect(blurEffect);

        boardWrap.getChildren().add(snapshotBlur);

        Timeline blurIn = new Timeline(
                new KeyFrame(Duration.millis(260), new KeyValue(blurEffect.radiusProperty(), BLUR_RADIUS))
        );
        blurIn.play();
    }
    // ── Entrance ─────────────────────────────────────────────────────

    private void playEntranceAnimation(VBox card) {
        FadeTransition overlayFade = new FadeTransition(Duration.millis(200), overlay);
        overlayFade.setToValue(1.0);

        ScaleTransition cardScale = new ScaleTransition(Duration.millis(220), card);
        cardScale.setToX(1.0);
        cardScale.setToY(1.0);
        cardScale.setInterpolator(Interpolator.EASE_OUT);
        cardScale.setDelay(Duration.millis(40));

        FadeTransition cardFade = new FadeTransition(Duration.millis(220), card);
        cardFade.setToValue(1.0);
        cardFade.setDelay(Duration.millis(40));

        new ParallelTransition(overlayFade, cardScale, cardFade).play();
    }

    // ── Card construction ───────────────────────────────────────────

    private VBox buildCard(GameOverInfo info, UserSession session,
                           Runnable onNewGame, Runnable onReturnToLobby) {
        VBox card = new VBox(0);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(CARD_SIZE, CARD_SIZE);
        card.setMinSize(CARD_SIZE, CARD_SIZE);
        card.setMaxSize(CARD_SIZE, CARD_SIZE);
        card.setStyle(
                "-fx-background-color: " + CARD_BG + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + CARD_BORDER + ";" +
                        "-fx-border-radius: 14;" +
                        "-fx-border-width: 1;"
        );

        Region topSpacer = new Region();
        Region bottomSpacer = new Region();
        VBox.setVgrow(topSpacer, Priority.ALWAYS);
        VBox.setVgrow(bottomSpacer, Priority.ALWAYS);

        card.getChildren().addAll(
                buildResultBanner(info.getResult(), info.getEndReason()),
                topSpacer,
                buildPlayerRow(info, session),
                bottomSpacer,
                buildDivider(),
                buildActionButtons(onNewGame, onReturnToLobby)
        );

        return card;
    }

    // ── Glow (static, no ongoing animation — cheap) ────────────────────

    private Color resultGlowColor(GameOverInfo.GameResult result) {
        return switch (result) {
            case WIN  -> Color.web(WIN_COLOR);
            case LOSS -> Color.web(LOSE_COLOR);
            case DRAW -> Color.web(DRAW_COLOR);
        };
    }

    private void applyGlow(VBox card, Color glowColor) {
        DropShadow glow = new DropShadow();
        glow.setColor(Color.color(glowColor.getRed(), glowColor.getGreen(), glowColor.getBlue(), 0.45));
        glow.setRadius(28);
        glow.setSpread(0.15);

        DropShadow depthShadow = new DropShadow();
        depthShadow.setColor(Color.color(0, 0, 0, 0.55));
        depthShadow.setRadius(20);
        depthShadow.setOffsetY(6);
        depthShadow.setInput(glow);

        card.setEffect(depthShadow);
    }

    // ── Top banner ───────────────────────────────────────────────────

    private VBox buildResultBanner(GameOverInfo.GameResult result, GameOverInfo.EndReason reason) {
        VBox banner = new VBox(6);
        banner.setAlignment(Pos.CENTER);
        banner.setPadding(new Insets(26, 20, 0, 20));

        String bannerColor = switch (result) {
            case WIN  -> WIN_COLOR;
            case LOSS -> LOSE_COLOR;
            case DRAW -> DRAW_COLOR;
        };

        String headline = switch (result) {
            case WIN  -> "You Won!";
            case LOSS -> "You Lost";
            case DRAW -> "Draw";
        };

        Label headlineLbl = new Label(headline);
        headlineLbl.setStyle(
                "-fx-text-fill: " + bannerColor + ";" +
                        "-fx-font-size: 26px;" +
                        "-fx-font-weight: 800;"
        );

        Label reasonLbl = new Label(formatReason(reason, result));
        reasonLbl.setStyle(
                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 600;"
        );

        banner.getChildren().addAll(headlineLbl, reasonLbl);
        return banner;
    }

    private String formatReason(GameOverInfo.EndReason reason, GameOverInfo.GameResult result) {
        return switch (reason) {
            case CHECKMATE              -> "by Checkmate";
            case RESIGNATION            -> result == GameOverInfo.GameResult.WIN ? "Opponent Resigned" : "by Resignation";
            case TIMEOUT                -> result == GameOverInfo.GameResult.WIN ? "Opponent ran out of time" : "on Time";
            case STALEMATE              -> "Stalemate";
            case DRAW_AGREEMENT         -> "by Agreement";
            case INSUFFICIENT_MATERIAL  -> "Insufficient Material";
            case REPETITION             -> "Threefold Repetition";
            case ABANDONED              -> "Opponent Abandoned";
        };
    }

    // ── Player row (avatar, name, elo before → after) ──────────────────

    private HBox buildPlayerRow(GameOverInfo info, UserSession session) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setMaxWidth(CARD_SIZE - 40);
        VBox.setMargin(row, new Insets(0, 20, 0, 20));
        row.setStyle(
                "-fx-background-color: " + ROW_BG + ";" +
                        "-fx-background-radius: 8;"
        );

        StackPane avatar = buildAvatar(session.getAvatarUrl(), initials(session.getUsername()),
                avatarColor(session.getUsername()), 40);

        VBox nameBlock = new VBox(3);
        nameBlock.setAlignment(Pos.CENTER_LEFT);

        Label nameLbl = new Label(session.getUsername());
        nameLbl.setStyle(
                "-fx-text-fill: " + TEXT_PRIMARY + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: 700;"
        );

        int eloBefore = session.getElo();
        int eloAfter  = info.getNewElo();
        int diff = eloAfter - eloBefore;

        String diffColor = diff > 0 ? WIN_COLOR : (diff < 0 ? LOSE_COLOR : TEXT_SECONDARY);
        String diffText  = diff > 0 ? ("+" + diff) : String.valueOf(diff);

        HBox eloRow = new HBox(4);
        eloRow.setAlignment(Pos.CENTER_LEFT);

        Label eloTextLbl = new Label(eloBefore + " → " + eloAfter);
        eloTextLbl.setStyle(
                "-fx-text-fill: " + TEXT_SECONDARY + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 600;"
        );

        Label diffLbl = new Label("(" + diffText + ")");
        diffLbl.setStyle(
                "-fx-text-fill: " + diffColor + ";" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: 800;"
        );

        eloRow.getChildren().addAll(eloTextLbl, diffLbl);
        nameBlock.getChildren().addAll(nameLbl, eloRow);

        row.getChildren().addAll(avatar, nameBlock);
        return row;
    }

    private Region buildDivider() {
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: " + CARD_BORDER + ";");
        VBox.setMargin(divider, new Insets(0, 20, 0, 20));
        return divider;
    }

    // ── Action buttons ────────────────────────────────────────────────

    private HBox buildActionButtons(Runnable onNewGame, Runnable onReturnToLobby) {
        HBox box = new HBox(10);
        box.setPadding(new Insets(14, 20, 20, 20));

        Button newGameBtn = buildButton("New Game", BTN_PRIMARY_BG, "#1a1a1a", onNewGame);
        Button returnBtn  = buildButton("Lobby", BTN_SECONDARY_BG, TEXT_PRIMARY, onReturnToLobby);

        HBox.setHgrow(newGameBtn, Priority.ALWAYS);
        HBox.setHgrow(returnBtn, Priority.ALWAYS);

        box.getChildren().addAll(newGameBtn, returnBtn);
        return box;
    }

    private Button buildButton(String text, String bgColor, String textColor, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                        "-fx-text-fill: " + textColor + ";" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: 700;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 9 0;" +
                        "-fx-cursor: hand;"
        );

        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        btn.setOnAction(e -> { if (action != null) action.run(); });

        return btn;
    }

    // ── Avatar helper ──────────────────────────────────────────────────

    private StackPane buildAvatar(String imageUrl, String initials, String bgColor, int size) {
        StackPane avatar = new StackPane();
        avatar.setPrefSize(size, size);
        avatar.setMinSize(size, size);
        avatar.setMaxSize(size, size);

        Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
        avatar.setClip(clip);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                Image img = new Image(imageUrl, size, size, true, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(size);
                iv.setFitHeight(size);
                avatar.getChildren().add(iv);
                return avatar;
            } catch (Exception ignored) {}
        }

        avatar.setStyle("-fx-background-color: " + bgColor + ";");
        Label lbl = new Label(initials);
        lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: " + (size / 3) + "px; -fx-font-weight: 700;");
        avatar.getChildren().add(lbl);
        return avatar;
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String initials(String username) {
        return Avatar.initials(username);
    }

    private String avatarColor(String username) {
        String[] palette = {
                "#7c5c3e", "#5c3e7c", "#3e7c5c",
                "#7c3e5c", "#3e5c7c", "#5c7c3e"
        };
        return palette[Math.abs(username.hashCode()) % palette.length];
    }
}