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

/**
 * Confirmation overlay shown when a player clicks "Offer Draw". Same shape
 * and styling as ResignConfirmCard — attaches itself on top of the given
 * parent StackPane, removes itself once the person picks Cancel or confirms.
 *
 * Usage (wiring up GameView's existing draw button):
 *
 *   drawBtn = buildActionBtn("½", DRAW_COLOR, "Offer draw", () -> {
 *       if (myTurn) return; // per the turn-restriction rule — button should already be disabled here
 *       new DrawOfferConfirmCard(root, () -> {
 *           stompSession.send("/app/game/" + gameId + "/offerDraw", new DrawOfferMessage(gameId));
 *       });
 *   });
 */
public class DrawOfferConfirmCard {

    // ── Palette (matches GameView's wood/gold theme) ───────────────────
    private static final String PANEL_TOP     = "#2a2018";
    private static final String PANEL_BOTTOM  = "#180f0a";
    private static final String CARD_BORDER   = "rgba(232,207,138,0.14)";
    private static final String ACCENT        = "#e8cf8a";
    private static final String TEXT_PRIMARY  = "#f5ece0";
    private static final String TEXT_SECONDARY= "#a08e78";
    private static final String DRAW_COLOR    = "#d8c8a8";
    private static final String BTN_BG        = "#332821";
    private static final String BTN_BG_HOVER  = "#443528";

    private final StackPane parent;
    private final Region scrim;
    private final VBox card;

    /**
     * @param parent          the StackPane to overlay this card onto (must already be in the scene)
     * @param onConfirmOffer  runs if the person confirms; the card removes itself either way
     */
    public DrawOfferConfirmCard(StackPane parent, Runnable onConfirmOffer) {
        this(parent, onConfirmOffer, null);
    }

    /**
     * @param parent          the StackPane to overlay this card onto
     * @param onConfirmOffer  runs if the person confirms offering a draw
     * @param onCancel        optional, runs if the person cancels (card is dismissed either way)
     */
    public DrawOfferConfirmCard(StackPane parent, Runnable onConfirmOffer, Runnable onCancel) {
        this.parent = parent;

        // ── Dim scrim behind the card ───────────────────────────────────
        scrim = new Region();
        scrim.setStyle("-fx-background-color: rgba(0,0,0,0.55);");
        scrim.prefWidthProperty().bind(parent.widthProperty());
        scrim.prefHeightProperty().bind(parent.heightProperty());
        scrim.setOnMouseClicked(e -> dismiss(onCancel)); // click outside = cancel

        // ── Card content ─────────────────────────────────────────────
        Label icon = new Label("½");
        icon.setStyle("-fx-text-fill: " + DRAW_COLOR + "; -fx-font-size: 28px; -fx-font-weight: 800;");

        Label title = new Label("Offer a draw?");
        title.setStyle("-fx-text-fill: " + TEXT_PRIMARY + "; -fx-font-size: 18px; -fx-font-weight: 800;");

        Label subtitle = new Label("Your opponent will be asked to accept or decline.");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(230);
        subtitle.setStyle("-fx-text-fill: " + TEXT_SECONDARY + "; -fx-font-size: 13px; -fx-font-weight: 500;");
        subtitle.setAlignment(Pos.CENTER);
        subtitle.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        VBox textBlock = new VBox(10, icon, title, subtitle);
        textBlock.setAlignment(Pos.CENTER);

        Region cancelBtn = buildButton("Cancel", BTN_BG, BTN_BG_HOVER, TEXT_PRIMARY,
                () -> dismiss(onCancel));
        Region offerBtn = buildButton("Offer Draw", DRAW_COLOR, "#c4b593", "#241a10",
                () -> dismiss(onConfirmOffer));

        HBox buttonRow = new HBox(10, cancelBtn, offerBtn);
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
        btn.setPadding(new Insets(10, 18, 10, 18));
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