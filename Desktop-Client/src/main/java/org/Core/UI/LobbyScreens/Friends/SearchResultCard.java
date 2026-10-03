
package org.Core.UI.LobbyScreens.Friends;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.Core.Social.DTO.UserSummary;
import org.Core.Social.FriendShipClient;

import javax.security.sasl.AuthenticationException;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.IntConsumer;


public final class SearchResultCard {

    private SearchResultCard() {}

    public static VBox build(UserSummary r,
                             FriendShipClient client,
                             IntConsumer onIncomingCountDelta,
                             IntConsumer onRequestRemoved,
                             Consumer<String> onError) {

        VBox card = new VBox(11);
        card.setPadding(new Insets(16));
        card.getStyleClass().add("search-result-card");

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        var avatar = Avatar.build(r.getAvatarUrl(), Avatar.initials(r.getUsername()),
                Avatar.colorFromName(r.getUsername()), 42);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(r.getUsername());
        name.getStyleClass().add("search-result-name");

        Label publicId = new Label(String.valueOf(r.getId()));
        publicId.getStyleClass().add("search-result-id");
        info.getChildren().addAll(name, publicId);
        top.getChildren().addAll(avatar, info);

        HBox eloRow = new HBox(6);
        eloRow.setAlignment(Pos.CENTER_LEFT);
        Label star = new Label("★");
        star.getStyleClass().add("search-result-star");
        Label eloVal = new Label(r.getElo() + " ELO");
        eloVal.getStyleClass().add("search-result-elo");
        eloRow.getChildren().addAll(star, eloVal);

        card.getChildren().addAll(top, eloRow);

        if (r.getIsFriend()) {
            card.getChildren().add(alreadyFriendsButton());

        } else if (r.getInvitationStatus() == UserSummary.InvitationStatus.SENT) {
            Button unsendBtn = new Button("✕ Unsend request");
            unsendBtn.setMaxWidth(Double.MAX_VALUE);
            unsendBtn.getStyleClass().add("btn-pending");
            unsendBtn.setOnAction(e -> handleUnsend(r, client, unsendBtn, onRequestRemoved, onError));
            card.getChildren().add(unsendBtn);

        } else if (r.getInvitationStatus() == UserSummary.InvitationStatus.RECEIVED) {
            HBox actions = new HBox(6);

            Button acceptBtn = new Button("✓ Accept");
            acceptBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(acceptBtn, Priority.ALWAYS);
            acceptBtn.getStyleClass().add("btn-accept-lg");
            acceptBtn.setOnAction(e -> {
                acceptBtn.setDisable(true);
                acceptBtn.setText("Accepting...");
                CompletableFuture
                        .runAsync(() -> runOrThrow(() -> client.accept(r.getId())))
                        .thenAccept(v -> Platform.runLater(() -> {
                            card.getChildren().remove(actions);
                            card.getChildren().add(alreadyFriendsButton());
                            onIncomingCountDelta.accept(-1);
                            onRequestRemoved.accept(r.getId());
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                acceptBtn.setDisable(false);
                                acceptBtn.setText("✓ Accept");
                                onError.accept(resolveError(ex));
                            });
                            return null;
                        });
            });

            Button rejectBtn = new Button("✕");
            rejectBtn.setPrefWidth(38);
            rejectBtn.getStyleClass().add("btn-reject-lg");
            rejectBtn.setOnAction(e -> {
                CompletableFuture
                        .runAsync(() -> runOrThrow(() -> client.reject(r.getId())))
                        .thenAccept(v -> Platform.runLater(() -> {
                            card.getChildren().remove(actions);
                            Button sendBtn = sendRequestButton(r, client, onRequestRemoved, onError);
                            card.getChildren().add(sendBtn);
                            onIncomingCountDelta.accept(-1);
                            onRequestRemoved.accept(r.getId());
                        }))
                        .exceptionally(ex -> {
                            Platform.runLater(() -> {
                                rejectBtn.setDisable(false);
                                onError.accept(resolveError(ex));
                            });
                            return null;
                        });
            });

            actions.getChildren().addAll(acceptBtn, rejectBtn);
            card.getChildren().add(actions);

        } else {
            card.getChildren().add(sendRequestButton(r, client, onRequestRemoved, onError));
        }

        return card;
    }

    private static Button alreadyFriendsButton() {
        Button btn = new Button("✓ Already friends");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("btn-already-friend");
        btn.setDisable(true);
        return btn;
    }

    private static Button sendRequestButton(UserSummary r, FriendShipClient client,
                                             IntConsumer onRequestRemoved, Consumer<String> onError) {
        Button sendBtn = new Button("+ Send friend request");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.getStyleClass().add("btn-add");
        sendBtn.setOnAction(e -> handleSend(r, client, sendBtn, onRequestRemoved, onError));
        return sendBtn;
    }

    private static void handleSend(UserSummary r, FriendShipClient client, Button btn,
                                    IntConsumer onRequestRemoved, Consumer<String> onError) {
        btn.setDisable(true);
        btn.setText("Sending...");
        CompletableFuture
                .runAsync(() -> runOrThrow(() -> client.invite(r.getId())))
                .thenAccept(v -> Platform.runLater(() -> {
                    btn.setText("✕ Unsend request");
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.getStyleClass().remove("btn-add");
                    btn.getStyleClass().add("btn-pending");
                    btn.setDisable(false);
                    btn.setOnAction(ev -> handleUnsend(r, client, btn, onRequestRemoved, onError));
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btn.setDisable(false);
                        btn.setText("+ Send friend request");
                        onError.accept(resolveError(ex));
                    });
                    return null;
                });
    }

    private static void handleUnsend(UserSummary r, FriendShipClient client, Button btn,
                                      IntConsumer onRequestRemoved, Consumer<String> onError) {
        btn.setDisable(true);
        btn.setText("Unsending...");
        CompletableFuture
                .runAsync(() -> runOrThrow(() -> client.unSend(r.getId())))
                .thenAccept(v -> Platform.runLater(() -> {
                    btn.setText("+ Send friend request");
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.getStyleClass().remove("btn-pending");
                    btn.getStyleClass().add("btn-add");
                    btn.setDisable(false);
                    btn.setOnAction(ev -> handleSend(r, client, btn, onRequestRemoved, onError));
                    onRequestRemoved.accept(r.getId());
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btn.setDisable(false);
                        btn.setText("✕ Unsend request");
                        onError.accept(resolveError(ex));
                    });
                    return null;
                });
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
