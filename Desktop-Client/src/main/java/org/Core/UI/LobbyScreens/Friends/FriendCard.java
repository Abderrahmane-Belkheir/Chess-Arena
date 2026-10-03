package org.Core.UI.LobbyScreens.Friends;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.Core.Social.DTO.FriendsList;
import org.Core.Social.FriendShipClient;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.IntConsumer;

public class FriendCard {

    private final StackPane overlay;
    private final VBox card;
    private final FriendsList.FriendEntry friend;
    private final FriendShipClient    client;
    private final IntConsumer onFriendRemoved;

    public FriendCard(FriendsList.FriendEntry friend,
                      FriendShipClient client,
                      StackPane overlay,
                      IntConsumer onFriendRemoved) {
        this.friend     = friend;
        this.client = client;
        this.overlay    = overlay;
        this.onFriendRemoved = onFriendRemoved;

        overlay.getStyleClass().add("modal-overlay");
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) dismiss(); });
        overlay.setAlignment(Pos.CENTER);

        card = new VBox(0);
        card.setPrefWidth(404);
        card.setMaxWidth(404);
        // StackPane stretches children to fill it unless maxHeight is capped —
        // VBox's default maxHeight is Double.MAX_VALUE, which is why the card
        // was filling the whole window vertically. USE_PREF_SIZE locks it back
        // to its content height.
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.getStyleClass().add("friend-card-modal");
        card.setOnMouseClicked(e -> e.consume());

        card.getChildren().addAll(
                buildHeader(),
                buildAvatarZone(),
                buildAction()
        );

        overlay.getChildren().add(card);
    }

    // ── Header ────────────────────────────────────────────────────────

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 0, 22));

        Label label = new Label("FRIEND");
        label.getStyleClass().add("modal-eyebrow");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setPrefSize(30, 30);
        closeBtn.getStyleClass().add("modal-close-btn");
        closeBtn.setOnAction(e -> dismiss());

        header.getChildren().addAll(label, spacer, closeBtn);
        return header;
    }

    // ── Avatar zone ───────────────────────────────────────────────────

    private HBox buildAvatarZone() {
        HBox zone = new HBox(22);
        zone.setAlignment(Pos.CENTER_LEFT);
        zone.setPadding(new Insets(26, 24, 26, 24));
        zone.getStyleClass().add("modal-section-border");

        // soft gold glow behind the avatar — echoes the hero panel's glow motif
        StackPane avatarGlowWrap = new StackPane();
        avatarGlowWrap.setPrefSize(148, 148);
        avatarGlowWrap.setMinSize(148, 148);
        avatarGlowWrap.setMaxSize(148, 148);
        Region glow = new Region();
        glow.setPrefSize(140, 140);
        glow.getStyleClass().add("modal-avatar-glow");

        // avatar
        StackPane avatarWrap = new StackPane();
        avatarWrap.setPrefSize(100, 100);
        avatarWrap.setMinSize(100, 100);
        avatarWrap.setMaxSize(100, 100);

        Region circle = new Region();
        circle.setPrefSize(94, 94);
        circle.setMinSize(94, 94);
        circle.setMaxSize(94, 94);
        circle.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 47;
            -fx-border-color: #3a352c;
            -fx-border-radius: 47;
            -fx-border-width: 2;
        """, avatarColor(friend.getUsername())));

        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        friend.getAvatarUrl(), 94, 94, true, true, true);
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(94);
                iv.setFitHeight(94);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(47, 47, 47);
                iv.setClip(clip);
                avatarWrap.getChildren().addAll(circle, iv);
            } catch (Exception ignored) {
                Label initials = buildInitialsLabel();
                avatarWrap.getChildren().addAll(circle, initials);
            }
        } else {
            Label initials = buildInitialsLabel();
            avatarWrap.getChildren().addAll(circle, initials);
        }

        // status ring around avatar — color matches status
        Region ring = new Region();
        ring.setPrefSize(100, 100);
        ring.setMinSize(100, 100);
        ring.setMaxSize(100, 100);
        ring.getStyleClass().add(statusRingClass());
        if (friend.getStatus() == FriendsList.Status.Offline) ring.setOpacity(0.3);
        avatarWrap.getChildren().add(ring);

        avatarGlowWrap.getChildren().addAll(glow, avatarWrap);

        // info
        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(friend.getUsername());
        name.getStyleClass().add("modal-username");

        // elo badge
        HBox eloBadge = new HBox(5);
        eloBadge.setAlignment(Pos.CENTER);
        eloBadge.setPadding(new Insets(3, 9, 3, 9));
        eloBadge.getStyleClass().add("modal-elo-badge");
        Label star = new Label("★");
        star.getStyleClass().add("modal-elo-star");
        Label eloVal = new Label(String.valueOf(friend.getElo()));
        eloVal.getStyleClass().add("modal-elo-value");
        eloBadge.getChildren().addAll(star, eloVal);

        // status label
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setPrefSize(7, 7);
        dot.getStyleClass().add(statusDotShapeClass());
        if (friend.getStatus() == FriendsList.Status.Offline) dot.setOpacity(0.4);
        Label statusTxt = new Label(statusLabel());
        statusTxt.getStyleClass().addAll("modal-status-text", statusAccentClass());
        if (friend.getStatus() == FriendsList.Status.Offline) statusTxt.setOpacity(0.5);
        statusRow.getChildren().addAll(dot, statusTxt);

        // friend id
        Label idLbl = new Label("ID: " + friend.getId());
        idLbl.getStyleClass().add("modal-id-text");

        info.getChildren().addAll(name, eloBadge, statusRow, idLbl);
        zone.getChildren().addAll(avatarGlowWrap, info);
        return zone;
    }

    private Label buildInitialsLabel() {
        Label lbl = new Label(initials(friend.getUsername()));
        lbl.getStyleClass().add("modal-avatar-initials");
        return lbl;
    }

    // ── Action button ─────────────────────────────────────────────────

    private VBox buildAction() {
        VBox wrap = new VBox(9);
        wrap.setPadding(new Insets(18, 22, 22, 22));

        switch (friend.getStatus()) {

            case InLobby -> {
                // Challenge to a game
                Button challengeBtn = buildPrimaryBtn("⚔  Challenge", "btn-challenge");
                ActionThrottle.install(challengeBtn, () -> {
                    dismiss();
                    CompletableFuture.runAsync(() -> {
                        try {
                            client.challenge(friend.getId());
                        } catch (IOException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                });
                wrap.getChildren().add(challengeBtn);
            }

            case InGame -> {
                // Spectate their current game
                Button spectateBtn = buildPrimaryBtn("👁  Spectate game", "btn-spectate");
                ActionThrottle.install(spectateBtn, () -> {
                    dismiss();
                    CompletableFuture.runAsync(() -> {
                        try {
                            client.spectate(friend.getId());
                        } catch (IOException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                });

                // still show challenge but greyed — so user understands why it's disabled
                Button challengeBtn = buildSecondaryBtn("⚔  Challenge  ·  in game");
                challengeBtn.setDisable(true);

                wrap.getChildren().addAll(spectateBtn, challengeBtn);
            }

            case Offline -> {
                // both disabled — show why
                Button spectateBtn = buildSecondaryBtn("👁  Spectate  ·  offline");
                spectateBtn.setDisable(true);

                Button challengeBtn = buildSecondaryBtn("⚔  Challenge  ·  offline");
                challengeBtn.setDisable(true);

                wrap.getChildren().addAll(challengeBtn, spectateBtn);
            }
        }

        // Remove friend — available regardless of status
        Button removeBtn = buildDangerBtn("Remove friend");
        removeBtn.setOnAction(e -> {
            dismiss();
            CompletableFuture
                    .runAsync(() -> {
                        try {
                            client.deleteFriend(friend.getId());
                        } catch (IOException | InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    })
                    .thenAccept(v -> Platform.runLater(() -> onFriendRemoved.accept(friend.getId())));
        });
        wrap.getChildren().add(removeBtn);

        return wrap;
    }

    private Button buildPrimaryBtn(String text, String styleClass) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add(styleClass);
        return btn;
    }

    private Button buildSecondaryBtn(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("btn-secondary");
        return btn;
    }

    private Button buildDangerBtn(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.getStyleClass().add("btn-danger");
        return btn;
    }

    // ── Show / dismiss ────────────────────────────────────────────────

    public void show() {
        overlay.setVisible(true);
        overlay.setOpacity(0);

        FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
        ft.setToValue(1.0);
        ft.play();

        card.setScaleX(0.93);
        card.setScaleY(0.93);
        ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
        st.setToX(1.0);
        st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);
        st.play();
    }

    public void dismiss() {
        FadeTransition ft = new FadeTransition(Duration.millis(160), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            overlay.setVisible(false);
            overlay.getChildren().remove(card);
        });
        ft.play();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private String statusAccentClass() {
        return switch (friend.getStatus()) {
            case InLobby -> "status-accent-inlobby";
            case InGame  -> "status-accent-ingame";
            case Offline -> "status-accent-offline";
        };
    }

    private String statusDotShapeClass() {
        return switch (friend.getStatus()) {
            case InLobby -> "status-dotshape-inlobby";
            case InGame  -> "status-dotshape-ingame";
            case Offline -> "status-dotshape-offline";
        };
    }

    private String statusRingClass() {
        return switch (friend.getStatus()) {
            case InLobby -> "status-ring-inlobby";
            case InGame  -> "status-ring-ingame";
            case Offline -> "status-ring-offline";
        };
    }

    private String statusLabel() {
        return switch (friend.getStatus()) {
            case InLobby -> "In lobby";
            case InGame  -> "In game";
            case Offline -> "Offline";
        };
    }

    private String initials(String u) {
        if (u == null || u.isEmpty()) return "?";
        String[] p = u.split("_");
        return p.length >= 2
                ? (p[0].substring(0, 1) + p[1].substring(0, 1)).toUpperCase()
                : u.substring(0, Math.min(2, u.length())).toUpperCase();
    }

    private String avatarColor(String u) {
        String[] palette = {"#7c5c3e","#5c3e7c","#3e7c5c","#7c3e5c","#3e5c7c","#5c7c3e"};
        return palette[Math.abs(u.hashCode()) % palette.length];
    }
}
