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

public class SpectateRequestCard {

    private static final Duration VISIBLE_DURATION = Duration.seconds(10);
    private static final Duration FADE_DURATION     = Duration.millis(200);


    private static final String WOOD_HIGHLIGHT = "#b98550";
    private static final String WOOD_MID       = "#7a4d29";
    private static final String WOOD_DARK      = "#3e2415";

    private final StackPane parent;
    private final VBox card;
    private boolean responded = false;

    public SpectateRequestCard(StackPane parent, SpectatedResponse request,
                               Consumer<Integer>  onAccept, Consumer<Integer> onDecline) {
        this(parent, request, onAccept, onDecline, null);
    }

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