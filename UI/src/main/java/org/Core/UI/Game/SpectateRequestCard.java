package org.Core.UI.Game;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.Core.Game.Events.SpectatedResponse;
import org.Core.UI.LobbyScreens.Friends.Avatar;

import java.util.function.Consumer;

import static org.Core.UI.LobbyScreens.Friends.Avatar.initials;

/**
 * Small toast shown when someone requests to spectate you — pinned to the
 * top-right of the given parent StackPane, auto-dismisses after 10 seconds
 * if ignored. Same shape/style as DrawOfferReceivedCard, but shows the
 * requester's actual avatar + username (from the SpectatedResponse payload)
 * instead of a generic icon, since "who's asking" is the whole point here.
 *
 * Usage (wherever the client receives a SpectatedResponse spectate-request event):
 *
 *   Platform.runLater(() ->
 *       new SpectateRequestCard(root, request,
 *           req -> stompSession.send("/app/spectate/respond",
 *                   new SpectateRespondMessage(req.getUserId(), true)),   // accept
 *           req -> stompSession.send("/app/spectate/respond",
 *                   new SpectateRespondMessage(req.getUserId(), false))   // decline
 *       )
 *   );
 */
public class SpectateRequestCard {

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
     * @param parent    the StackPane to pin this toast onto (top-right corner)
     * @param request   who's asking — carries userId/username/avatarUrl
     * @param onAccept  runs with the request if accepted within the 10s window
     * @param onDecline runs with the request if explicitly declined within the 10s window
     *                  (NOT called on silent timeout — see the onTimeout overload for that)
     */
    public SpectateRequestCard(StackPane parent, SpectatedResponse request,
                               Consumer<Integer>  onAccept, Consumer<Integer> onDecline) {
        this(parent, request, onAccept, onDecline, null);
    }

    /**
     * @param onTimeout optional, runs with the request only if the 10s window elapses
     *                  with no response. The server-side pending-request TTL is the
     *                  authoritative lapse — this is only here if you want the UI to
     *                  reflect it too (e.g. logging, or letting the requester know).
     */
    public SpectateRequestCard(StackPane parent, SpectatedResponse request,
                                Consumer<Integer> onAccept, Consumer<Integer> onDecline,
                                Consumer<SpectatedResponse> onTimeout) {
        this.parent = parent;

        StackPane avatar = Avatar.build(request.getAvatarUrl(), initials(request.getUsername()),
                Avatar.colorFromName(request.getUsername()), 22);

        Label message = new Label(request.getUsername() + " wants to spectate");
        message.setWrapText(true);
        message.setMaxWidth(88);
        message.setStyle("-fx-text-fill: #241a10; -fx-font-size: 9px; -fx-font-weight: 700;");

        HBox header = new HBox(6, avatar, message);
        header.setAlignment(Pos.CENTER_LEFT);

        Region declineBtn = buildButton("✕", "rgba(36,26,16,0.12)", "rgba(36,26,16,0.22)", "#241a10",
                () -> respond(request, onDecline));
        Region acceptBtn = buildButton("✓", "#241a10", "#3a2a18", "#e8d8b8",
                () -> respond(request, onAccept));

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
        scheduleAutoDismiss(request, onTimeout);
    }

    private void playEntrance() {
        card.setOpacity(0);
        card.setTranslateX(16);
        FadeTransition fadeIn = new FadeTransition(FADE_DURATION, card);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void scheduleAutoDismiss(SpectatedResponse request, Consumer<SpectatedResponse> onTimeout) {
        PauseTransition wait = new PauseTransition(VISIBLE_DURATION);
        FadeTransition fadeOut = new FadeTransition(FADE_DURATION, card);
        fadeOut.setToValue(0);

        SequentialTransition timeout = new SequentialTransition(wait, fadeOut);
        timeout.setOnFinished(e -> {
            if (!responded) {
                remove();
                if (onTimeout != null) onTimeout.accept(request);
            }
        });
        timeout.play();
    }

    /** request is captured from the constructor via the two respond(...) call sites below. */
    private void respond(SpectatedResponse request, Consumer<Integer> action) {
        if (responded) return; // guard against double-click during the fade
        responded = true;
        remove();
        if (action != null) action.accept(request.getUserId());
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