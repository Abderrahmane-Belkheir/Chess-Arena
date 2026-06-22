
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
                             Consumer<String> onError) {

        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setStyle("""
            -fx-background-color: #141414;
            -fx-border-color: transparent transparent #1e1e1e transparent;
            -fx-border-width: 1;
        """);

        HBox top = new HBox(12);
        top.setAlignment(Pos.CENTER_LEFT);

        var avatar = Avatar.build(r.getAvatarUrl(), Avatar.initials(r.getUsername()),
                Avatar.colorFromName(r.getUsername()));

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(r.getUsername());
        name.setStyle("""
            -fx-text-fill: #f0f0f0;
            -fx-font-size: 13px;
            -fx-font-weight: 700;
        """);

        Label publicId = new Label(String.valueOf(r.getId()));
        publicId.setStyle("""
            -fx-text-fill: #444444;
            -fx-font-size: 10px;
            -fx-font-weight: 600;
        """);
        info.getChildren().addAll(name, publicId);
        top.getChildren().addAll(avatar, info);

        HBox eloRow = new HBox(6);
        eloRow.setAlignment(Pos.CENTER_LEFT);
        Label star = new Label("★");
        star.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 11px;");
        Label eloVal = new Label(r.getElo() + " ELO");
        eloVal.setStyle("""
            -fx-text-fill: #888888;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
        """);
        eloRow.getChildren().addAll(star, eloVal);

        card.getChildren().addAll(top, eloRow);

        if (r.getIsFriend()) {
            card.getChildren().add(alreadyFriendsButton());

        } else if (r.getInvitationStatus() == UserSummary.InvitationStatus.SENT) {
            Button unsendBtn = new Button("✕ Unsend request");
            unsendBtn.setMaxWidth(Double.MAX_VALUE);
            unsendBtn.setStyle(Styles.pendingBtnStyle());
            unsendBtn.setOnMouseEntered(e -> unsendBtn.setOpacity(0.80));
            unsendBtn.setOnMouseExited(e  -> unsendBtn.setOpacity(1.0));
            unsendBtn.setOnAction(e -> handleUnsend(r, client, unsendBtn, onError));
            card.getChildren().add(unsendBtn);

        } else if (r.getInvitationStatus() == UserSummary.InvitationStatus.RECEIVED) {
            HBox actions = new HBox(6);

            Button acceptBtn = new Button("✓ Accept");
            acceptBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(acceptBtn, Priority.ALWAYS);
            acceptBtn.setStyle(Styles.acceptBtnStyleLarge());
            acceptBtn.setOnMouseEntered(e -> acceptBtn.setOpacity(0.88));
            acceptBtn.setOnMouseExited(e  -> acceptBtn.setOpacity(1.0));
            acceptBtn.setOnAction(e -> {
                acceptBtn.setDisable(true);
                acceptBtn.setText("Accepting...");
                CompletableFuture
                        .runAsync(() -> runOrThrow(() -> client.accept(r.getId())))
                        .thenAccept(v -> Platform.runLater(() -> {
                            card.getChildren().remove(actions);
                            card.getChildren().add(alreadyFriendsButton());
                            onIncomingCountDelta.accept(-1);
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
            rejectBtn.setStyle(Styles.rejectBtnStyleLarge());
            rejectBtn.setOnMouseEntered(e -> rejectBtn.setStyle(
                    rejectBtn.getStyle().replace("#1e1e1e", "#2a2a2a")
                            .replace("#666666", "#ffffff")));
            rejectBtn.setOnMouseExited(e -> rejectBtn.setStyle(
                    rejectBtn.getStyle().replace("#2a2a2a;", """
                        #1e1e1e;
                        -fx-border-color: #2a2a2a;
                    """).replace("#ffffff", "#666666")));
            rejectBtn.setOnAction(e -> {
                CompletableFuture
                        .runAsync(() -> runOrThrow(() -> client.reject(r.getId())))
                        .thenAccept(v -> Platform.runLater(() -> {
                            card.getChildren().remove(actions);
                            Button sendBtn = sendRequestButton(r, client, onError);
                            card.getChildren().add(sendBtn);
                            onIncomingCountDelta.accept(-1);
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
            card.getChildren().add(sendRequestButton(r, client, onError));
        }

        return card;
    }

    private static Button alreadyFriendsButton() {
        Button btn = new Button("✓ Already friends");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(Styles.friendBtnStyle());
        btn.setDisable(true);
        return btn;
    }

    private static Button sendRequestButton(UserSummary r, FriendShipClient client, Consumer<String> onError) {
        Button sendBtn = new Button("+ Send friend request");
        sendBtn.setMaxWidth(Double.MAX_VALUE);
        sendBtn.setStyle(Styles.addBtnStyle());
        sendBtn.setOnAction(e -> handleSend(r, client, sendBtn, onError));
        return sendBtn;
    }

    private static void handleSend(UserSummary r, FriendShipClient client, Button btn, Consumer<String> onError) {
        btn.setDisable(true);
        btn.setText("Sending...");
        CompletableFuture
                .runAsync(() -> runOrThrow(() -> client.invite(r.getId())))
                .thenAccept(v -> Platform.runLater(() -> {
                    btn.setText("✕ Unsend request");
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setStyle(Styles.pendingBtnStyle());
                    btn.setDisable(false);
                    btn.setOnMouseEntered(e -> btn.setOpacity(0.80));
                    btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
                    btn.setOnAction(ev -> handleUnsend(r, client, btn, onError));
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

    private static void handleUnsend(UserSummary r, FriendShipClient client, Button btn, Consumer<String> onError) {
        btn.setDisable(true);
        btn.setText("Unsending...");
        CompletableFuture
                .runAsync(() -> runOrThrow(() -> client.unSend(r.getId())))
                .thenAccept(v -> Platform.runLater(() -> {
                    btn.setText("+ Send friend request");
                    btn.setMaxWidth(Double.MAX_VALUE);
                    btn.setStyle(Styles.addBtnStyle());
                    btn.setDisable(false);
                    btn.setOnAction(ev -> handleSend(r, client, btn, onError));
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