package org.Core.UI.LobbyScreens.Friends;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.Core.Social.DTO.FriendsList;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

/**
 * A single friend — its own small bordered/rounded card (not a flat row in
 * a shared list), matching Chess-UI-Refactoring.png: a status dot badge
 * overlapping the avatar's bottom-right edge (not sitting beside it), and an
 * action button on the right whose label follows the friend's own status —
 * "Watch" while they're in a game, "Challenge" while they're online and
 * free, nothing while offline.
 */
public final class FriendRow {

    private FriendRow() {}

    public static HBox build(FriendsList.FriendEntry f, FriendShipClient client, LobbyController controller,
                              IntConsumer onFriendRemoved) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(9, 10, 9, 9));
        row.getStyleClass().add("friend-row");

        if (f.getStatus() == FriendsList.Status.Offline) {
            row.setOpacity(0.5);
        } else {
            row.getStyleClass().add("hoverable");
        }

        // Status dot as a badge overlapping the avatar's bottom-right edge,
        // not a separate dot floating beside it.
        StackPane avatarBadge = new StackPane();
        avatarBadge.setPickOnBounds(false);
        var avatar = Avatar.build(f.getAvatarUrl(), Avatar.initials(f.getUsername()), f.getAvatarColor(), 36);

        Region statusDot = new Region();
        statusDot.setPrefSize(11, 11);
        // Region's default maxSize is unbounded, so inside a StackPane it
        // would otherwise be stretched to fill (and hide) the whole avatar
        // instead of staying a small badge in the corner.
        statusDot.setMaxSize(11, 11);
        statusDot.getStyleClass().add(statusDotClass(f.getStatus()));
        StackPane.setAlignment(statusDot, Pos.BOTTOM_RIGHT);
        statusDot.setTranslateX(-1);
        statusDot.setTranslateY(-1);

        avatarBadge.getChildren().addAll(avatar, statusDot);

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label name = new Label(f.getUsername());
        name.getStyleClass().add("friend-name");

        Label elo = new Label(f.getStatus() == FriendsList.Status.InGame
                ? "In game · " + f.getElo()
                : String.valueOf(f.getElo()));
        elo.getStyleClass().add(f.getStatus() == FriendsList.Status.InGame ? "friend-elo-ingame" : "friend-elo");
        info.getChildren().addAll(name, elo);

        row.getChildren().addAll(avatarBadge, info);

        // Action button follows the friend's status — Watch (spectate) while
        // they're in a game, Challenge while online and free to play,
        // nothing while offline.
        if (f.getStatus() == FriendsList.Status.InGame) {
            Button watch = new Button("◎  Watch");
            watch.getStyleClass().add("btn-watch");
            watch.setOnMouseClicked(javafx.event.Event::consume);
            ActionThrottle.install(watch, () -> CompletableFuture.runAsync(() -> {
                try {
                    client.spectate(f.getId());
                } catch (IOException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }));
            row.getChildren().add(watch);
        } else if (f.getStatus() == FriendsList.Status.InLobby) {
            Button challenge = new Button("♞  Challenge");
            challenge.getStyleClass().add("btn-invite");
            challenge.setOnMouseClicked(javafx.event.Event::consume);
            ActionThrottle.install(challenge, () -> CompletableFuture.runAsync(() -> {
                try {
                    client.challenge(f.getId());
                } catch (IOException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }));
            row.getChildren().add(challenge);
        }

        row.setOnMouseClicked(e -> {
            StackPane overlay = controller.getOverlay();
            FriendCard card = new FriendCard(f, client, overlay, onFriendRemoved);
            card.show();
        });
        return row;
    }

    private static String statusDotClass(FriendsList.Status status) {
        return switch (status) {
            case InGame  -> "status-dot-ingame";
            case InLobby -> "status-dot-inlobby";
            case Offline -> "status-dot-offline";
        };
    }
}
