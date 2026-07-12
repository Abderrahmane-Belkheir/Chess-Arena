
package org.Core.UI.Game;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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
 * Small toast shown to the RECEIVER of a draw offer — pinned to the top-right
 * of the given parent StackPane, auto-dismisses after 10 seconds if ignored.
 * Unlike ResignConfirmCard/DrawOfferConfirmCard, this is non-modal: no scrim,
 * doesn't block the board, since the receiver should still be free to just
 * keep playing (their move implicitly declines the offer server-side anyway).
 *
 * Usage (wherever the client receives the "opponent offered a draw" STOMP event):
 *
 *   Platform.runLater(() ->
 *       new DrawOfferReceivedCard(root,
 *           () -> stompSession.send("/app/game/" + gameId + "/respondDraw",
 *                   new DrawResponseMessage(gameId, true)),   // accept
 *           () -> stompSession.send("/app/game/" + gameId + "/respondDraw",
 *                   new DrawResponseMessage(gameId, false))   // decline
 *       )
 *   );
 */
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Small toast shown to the RECEIVER of a draw offer — pinned to the top-right
 * of the given parent StackPane, auto-dismisses after 10 seconds if ignored.
 * Unlike ResignConfirmCard/DrawOfferConfirmCard, this is non-modal: no scrim,
 * doesn't block the board, since the receiver should still be free to just
 * keep playing (their move implicitly declines the offer server-side anyway).
 *
 * Usage (wherever the client receives the "opponent offered a draw" STOMP event):
 *
 *   Platform.runLater(() ->
 *       new DrawOfferReceivedCard(root,
 *           () -> stompSession.send("/app/game/" + gameId + "/respondDraw",
 *                   new DrawResponseMessage(gameId, true)),   // accept
 *           () -> stompSession.send("/app/game/" + gameId + "/respondDraw",
 *                   new DrawResponseMessage(gameId, false))   // decline
 *       )
 *   );
 */
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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
 * Small toast shown to the RECEIVER of a draw offer — pinned to the top-right
 * of the given parent StackPane, auto-dismisses after 10 seconds if ignored.
 * Unlike ResignConfirmCard/DrawOfferConfirmCard, this is non-modal: no scrim,
 * doesn't block the board, since the receiver should still be free to just
 * keep playing (their move implicitly declines the offer server-side anyway).
 *
 * Usage (wherever the client receives the "opponent offered a draw" STOMP event):
 *
 *   Platform.runLater(() ->
 *       new DrawOfferReceivedCard(root,
 *           () -> stompSession.send("/app/game/" + gameId + "/respondDraw",
 *                   new DrawResponseMessage(gameId, true)),   // accept
 *           () -> stompSession.send("/app/game/" + gameId + "/respondDraw",
 *                   new DrawResponseMessage(gameId, false))   // decline
 *       )
 *   );
 */
public class DrawOfferReceivedCard {

    private static final Duration VISIBLE_DURATION = Duration.seconds(10);
    private static final Duration FADE_DURATION     = Duration.millis(200);

    // ── Palette (matches GameView's wood/gold theme) ───────────────────
    private static final String WOOD_HIGHLIGHT = "#b98550";
    private static final String WOOD_MID       = "#7a4d29";
    private static final String WOOD_DARK      = "#3e2415";

    private final StackPane parent;
    private final VBox card;
    private boolean responded = false;

    /**
     * @param parent    the StackPane to pin this toast onto (top-left corner)
     * @param onAccept  runs if the receiver accepts within the 10s window
     * @param onDecline runs if the receiver explicitly declines within the 10s window
     *                  (NOT called on silent timeout — see onTimeout overload if you need that distinction)
     */
    public DrawOfferReceivedCard(StackPane parent, Runnable onAccept, Runnable onDecline) {
        this(parent, onAccept, onDecline, null);
    }

    /**
     * @param onTimeout optional, runs only if the 10s window elapses with no response.
     *                  Usually you don't need this client-side — the server-side TTL
     *                  on the draw offer already handles the authoritative lapse — but
     *                  it's here in case you want to reflect it in the UI too (e.g. a toast).
     */
    public DrawOfferReceivedCard(StackPane parent, Runnable onAccept, Runnable onDecline, Runnable onTimeout) {
        this.parent = parent;

        Label icon = new Label("½");
        icon.setStyle("-fx-text-fill: #241a10; -fx-font-size: 14px; -fx-font-weight: 800;");

        Label message = new Label("Draw offered");
        message.setStyle("-fx-text-fill: #241a10; -fx-font-size: 10px; -fx-font-weight: 700;");

        HBox header = new HBox(4, icon, message);
        header.setAlignment(Pos.CENTER);

        Region declineBtn = buildButton("✕", "rgba(36,26,16,0.12)", "rgba(36,26,16,0.22)", "#241a10",
                () -> respond(onDecline));
        Region acceptBtn = buildButton("✓", "#241a10", "#3a2a18", "#e8d8b8",
                () -> respond(onAccept));

        HBox buttonRow = new HBox(6, declineBtn, acceptBtn);
        buttonRow.setAlignment(Pos.CENTER);

        card = new VBox(6, header, buttonRow);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(6, 8, 6, 8));
        card.setPrefSize(150, 95);
        card.setMinSize(150, 95);
        card.setMaxSize(150, 95);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, " + WOOD_HIGHLIGHT + ", " + WOOD_MID + ");" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: " + WOOD_DARK + ";" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 10;"
        );
        card.setEffect(new DropShadow(16, Color.color(0, 0, 0, 0.5)));

        StackPane.setAlignment(card, Pos.TOP_RIGHT);
        StackPane.setMargin(card, new Insets(16));

        parent.getChildren().add(card);
        playEntrance();
        scheduleAutoDismiss(onTimeout);
    }

    private void playEntrance() {
        card.setOpacity(0);
        card.setTranslateX(16);
        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, card);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void scheduleAutoDismiss(Runnable onTimeout) {
        PauseTransition wait = new PauseTransition(VISIBLE_DURATION);
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, card);
        fadeOut.setToValue(0);

        SequentialTransition timeout = new SequentialTransition(wait, fadeOut);
        timeout.setOnFinished(e -> {
            if (!responded) {
                remove();
                if (onTimeout != null) onTimeout.run();
            }
        });
        timeout.play();
    }

    private void respond(Runnable action) {
        if (responded) return; // guard against double-click during the fade
        responded = true;
        remove();
        if (action != null) action.run();
    }

    private void remove() {
        parent.getChildren().remove(card);
    }

    private Region buildButton(String text, String bg, String hoverBg, String textColor, Runnable action) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: " + textColor + "; -fx-font-size: 10px; -fx-font-weight: 700;");

        StackPane btn = new StackPane(lbl);
        btn.setPadding(new Insets(4, 8, 4, 8));
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
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;";
    }
}