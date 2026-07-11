package org.Core.UI.Game;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;


import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;


public class ResignConfirmCard {

    // ── Palette (matches GameView's wood/gold theme) ───────────────────
    private static final String PANEL_TOP     = "#2a2018";
    private static final String PANEL_BOTTOM  = "#180f0a";
    private static final String CARD_BORDER   = "rgba(232,207,138,0.14)";
    private static final String ACCENT        = "#e8cf8a";
    private static final String TEXT_PRIMARY  = "#f5ece0";
    private static final String TEXT_SECONDARY= "#a08e78";
    private static final String RESIGN_COLOR  = "#e0645a";
    private static final String BTN_BG        = "#332821";
    private static final String BTN_BG_HOVER  = "#443528";

    private final StackPane parent;
    private final Region scrim;
    private final VBox card;

    public ResignConfirmCard(StackPane parent, Runnable onConfirmResign) {
        this(parent, onConfirmResign, null);
    }

    /**
     * @param parent          the StackPane to overlay this card onto
     * @param onConfirmResign runs if the person confirms resignation
     * @param onCancel        optional, runs if the person cancels (card is dismissed either way)
     */
    public ResignConfirmCard(StackPane parent, Runnable onConfirmResign, Runnable onCancel) {
        this.parent = parent;

        // ── Dim scrim behind the card ───────────────────────────────────
        scrim = new Region();
        scrim.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        scrim.prefWidthProperty().bind(parent.widthProperty());
        scrim.prefHeightProperty().bind(parent.heightProperty());
        scrim.setOnMouseClicked(e -> dismiss(onCancel)); // click outside = cancel

        // ── Card content ─────────────────────────────────────────────
        Label icon = new Label("⚑");
        icon.setStyle("-fx-text-fill: " + RESIGN_COLOR + "; -fx-font-size: 28px;");

        Label title = new Label("Resign the game?");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: 800;");

        Label subtitle = new Label("This will end the game immediately as a loss. This can't be undone.");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(230);
        subtitle.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-font-weight: 500;");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox textBlock = new VBox(10, icon, title, subtitle);
        textBlock.setAlignment(Pos.CENTER);

        Region cancelBtn = buildButton("Cancel", BTN_BG, BTN_BG_HOVER, TEXT_PRIMARY,
                () -> dismiss(onCancel));
        Region resignBtn = buildButton("Resign", RESIGN_COLOR, "#c94f45", "#ffffff",
                () -> dismiss(onConfirmResign));

        HBox buttonRow = new HBox(10, cancelBtn, resignBtn);
        buttonRow.setAlignment(Pos.CENTER);

        card = new VBox(16, textBlock, buttonRow);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(24));
        card.setPrefSize(300, 300);
        card.setMinSize(300, 300);
        card.setMaxSize(300, 300);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + PANEL_TOP + ", " + PANEL_BOTTOM + ");" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: " + CARD_BORDER + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 14;"
        );
        card.setEffect(new DropShadow(30, Color.color(0, 0, 0, 0.6)));
        card.setOnMouseClicked(javafx.event.Event::consume); // don't let clicks fall through to scrim

        StackPane.setAlignment(card, Pos.CENTER);

        parent.getChildren().addAll(scrim, card);
        playEntrance();
    }

    private void playEntrance() {
        scrim.setOpacity(0);
        card.setOpacity(0);
        card.setScaleX(0.94);
        card.setScaleY(0.94);

        FadeTransition scrimFade = new FadeTransition(Duration.millis(160), scrim);
        scrimFade.setToValue(1);

        FadeTransition cardFade = new FadeTransition(Duration.millis(160), card);
        cardFade.setToValue(1);

        ScaleTransition cardScale = new ScaleTransition(Duration.millis(160), card);
        cardScale.setToX(1);
        cardScale.setToY(1);

        scrimFade.play();
        cardFade.play();
        cardScale.play();
    }

    private void dismiss(Runnable then) {
        parent.getChildren().removeAll(scrim, card);
        if (then != null) then.run();
    }

    private Region buildButton(String text, String bg, String hoverBg, String textColor, Runnable action) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 13px; -fx-font-weight: 700;");

        StackPane btn = new StackPane(lbl);
        btn.setPadding(new Insets(10, 22, 10, 22));
        btn.setStyle(buttonStyle(bg));
        btn.setOnMouseEntered(e -> btn.setStyle(buttonStyle(hoverBg)));
        btn.setOnMouseExited(e -> btn.setStyle(buttonStyle(bg)));
        btn.setOnMouseClicked(e -> {
            e.consume();
            action.run();
        });
        return btn;
    }

    private String buttonStyle(String bg) {
        return "-fx-background-color: " + bg + ";" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;";
    }
}