
package org.Core.UI.LobbyScreens.Friends;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.Core.Social.DTO.InvitationsList;
import org.Core.Social.FriendShipClient;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class RequestRow {

    private RequestRow() {}

    public static VBox build(InvitationsList.InvitationEntry r,
                             FriendShipClient client,
                             Consumer<VBox> onRemoved,
                             Runnable onPendingDelta,
                             Consumer<String> onError) {

        VBox card = new VBox(10);
        card.setPadding(new Insets(12, 14, 12, 14));
        card.getStyleClass().add("request-card");

        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);

        var avatar = Avatar.build(r.getAvatarUrl(), Avatar.initials(r.getUsername()), r.getAvatarColor(), 40);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(r.getUsername());
        name.getStyleClass().add("request-name");
        Label eloLbl = new Label(r.getElo() + " ELO");
        eloLbl.getStyleClass().add("request-elo");
        info.getChildren().addAll(name, eloLbl);
        top.getChildren().addAll(avatar, info);
        card.getChildren().add(top);

        if (r.isIncoming()) {
            HBox actions = new HBox(6);

            Button accept = new Button("✓ Accept");
            accept.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(accept, Priority.ALWAYS);
            accept.getStyleClass().add("btn-accept-sm");
            accept.setOnAction(e -> {
                accept.setDisable(true);
                CompletableFuture
                        .runAsync(() -> runOrThrow(() -> client.accept(parsePublicId(r))))
                        .thenAccept(v -> Platform.runLater(() -> {
                            onRemoved.accept(card);
                            onPendingDelta.run();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                accept.setDisable(false);
                                onError.accept(resolveError(ex));
                            });
                            return null;
                        });
            });

            Button reject = new Button("✕");
            reject.setPrefWidth(34);
            reject.getStyleClass().add("btn-reject-sm");
            reject.setOnAction(e -> {
                reject.setDisable(true);
                CompletableFuture
                        .runAsync(() -> runOrThrow(() -> client.reject(parsePublicId(r))))
                        .thenAccept(v -> Platform.runLater(() -> {
                            onRemoved.accept(card);
                            onPendingDelta.run();
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                reject.setDisable(false);
                                onError.accept(resolveError(ex));
                            });
                            return null;
                        });
            });

            actions.getChildren().addAll(accept, reject);
            card.getChildren().add(actions);

        } else {
            Button cancel = new Button("Cancel request");
            cancel.setMaxWidth(Double.MAX_VALUE);
            cancel.getStyleClass().add("btn-cancel-request");
            cancel.setOnAction(e -> {
                cancel.setDisable(true);
                CompletableFuture
                        .runAsync(() -> runOrThrow(() -> client.unSend(parsePublicId(r))))
                        .thenAccept(v -> Platform.runLater(() -> onRemoved.accept(card)))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                cancel.setDisable(false);
                                onError.accept(resolveError(ex));
                            });
                            return null;
                        });
            });
            card.getChildren().add(cancel);
        }

        return card;
    }

    private static int parsePublicId(InvitationsList.InvitationEntry r) {
        return r.getPublicId() != null ? Integer.parseInt(r.getPublicId()) : 0;
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws IOException, InterruptedException;
    }

    private static void runOrThrow(ThrowingRunnable action) {
        try {
            action.run();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static String resolveError(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        if (cause instanceof AuthenticationException)
            return "Session expired. Please log in again.";
        return "Request failed. Check your connection.";
    }
}
