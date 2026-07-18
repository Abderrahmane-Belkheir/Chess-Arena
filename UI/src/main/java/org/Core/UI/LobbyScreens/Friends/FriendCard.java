package org.Core.UI.LobbyScreens.Friends;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.Core.Social.DTO.FriendsPage;
import org.Core.Social.FriendShipClient;

public class FriendCard {

    private final StackPane overlay;
    private final VBox card;
    private final FriendsPage.FriendEntry friend;
    private final FriendShipClient    client;

    public FriendCard(FriendsPage.FriendEntry friend,
                      FriendShipClient client,
                      StackPane overlay) {
        this.friend     = friend;
        this.client = client;
        this.overlay    = overlay;

        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.72);");
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) dismiss(); });
        overlay.setAlignment(Pos.CENTER);

        card = new VBox(0);
        card.setPrefWidth(380);
        card.setMaxWidth(380);
        // StackPane stretches children to fill it unless maxHeight is capped —
        // VBox's default maxHeight is Double.MAX_VALUE, which is why the card
        // was filling the whole window vertically. USE_PREF_SIZE locks it back
        // to its content height.
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle("""
            -fx-background-color: #141412;
            -fx-background-radius: 18;
            -fx-border-color: #252522;
            -fx-border-radius: 18;
            -fx-border-width: 1;
        """);
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
        header.setPadding(new Insets(16, 20, 0, 20));

        Label label = new Label("FRIEND");
        label.setStyle("""
            -fx-text-fill: #444444;
            -fx-font-size: 12px;
            -fx-font-weight: bold;
        """);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setPrefSize(28, 28);
        closeBtn.setStyle(closeBtnStyle(false));
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(closeBtnStyle(true)));
        closeBtn.setOnMouseExited(e  -> closeBtn.setStyle(closeBtnStyle(false)));
        closeBtn.setOnAction(e -> dismiss());

        header.getChildren().addAll(label, spacer, closeBtn);
        return header;
    }

    // ── Avatar zone ───────────────────────────────────────────────────

    private HBox buildAvatarZone() {
        HBox zone = new HBox(20);
        zone.setAlignment(Pos.CENTER_LEFT);
        zone.setPadding(new Insets(22, 20, 22, 20));
        zone.setStyle("""
            -fx-border-color: transparent transparent #1e1e1b transparent;
            -fx-border-width: 1;
        """);

        // avatar
        StackPane avatarWrap = new StackPane();
        avatarWrap.setPrefSize(88, 88);
        avatarWrap.setMinSize(88, 88);
        avatarWrap.setMaxSize(88, 88);

        Region circle = new Region();
        circle.setPrefSize(84, 84);
        circle.setMinSize(84, 84);
        circle.setMaxSize(84, 84);
        circle.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 42;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 42;
            -fx-border-width: 2;
        """, avatarColor(friend.getUsername())));

        if (friend.getAvatarUrl() != null && !friend.getAvatarUrl().isEmpty()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        friend.getAvatarUrl(), 84, 84, true, true, true);
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(84);
                iv.setFitHeight(84);
                javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(42, 42, 42);
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
        ring.setPrefSize(88, 88);
        ring.setMinSize(88, 88);
        ring.setMaxSize(88, 88);
        ring.setStyle(String.format("""
            -fx-background-color: transparent;
            -fx-background-radius: 44;
            -fx-border-color: %s;
            -fx-border-radius: 44;
            -fx-border-width: 2;
        """, statusAccentColor()));
        if (friend.getStatus() == FriendsPage.Status.Offline) ring.setOpacity(0.3);
        avatarWrap.getChildren().add(ring);

        // info
        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(friend.getUsername());
        name.setStyle("""
            -fx-text-fill: #f0f0f0;
            -fx-font-size: 19px;
            -fx-font-weight: 900;
        """);

        // elo badge
        HBox eloBadge = new HBox(5);
        eloBadge.setAlignment(Pos.CENTER);
        eloBadge.setPadding(new Insets(3, 9, 3, 9));
        eloBadge.setStyle("""
            -fx-background-color: rgba(230,184,74,0.1);
            -fx-background-radius: 6;
            -fx-border-color: rgba(230,184,74,0.2);
            -fx-border-radius: 6;
            -fx-border-width: 1;
        """);
        Label star = new Label("★");
        star.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 12px;");
        Label eloVal = new Label(String.valueOf(friend.getElo()));
        eloVal.setStyle("-fx-text-fill: #e6b84a; -fx-font-size: 13px; -fx-font-weight: 700;");
        eloBadge.getChildren().addAll(star, eloVal);

        // status label
        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setPrefSize(7, 7);
        dot.setStyle("-fx-background-color: " + statusAccentColor()
                + "; -fx-background-radius: 3.5;");
        if (friend.getStatus() == FriendsPage.Status.Offline) dot.setOpacity(0.4);
        Label statusTxt = new Label(statusLabel());
        statusTxt.setStyle("-fx-text-fill: " + statusAccentColor()
                + "; -fx-font-size: 12px; -fx-font-weight: 700;");
        if (friend.getStatus() == FriendsPage.Status.Offline) statusTxt.setOpacity(0.5);
        statusRow.getChildren().addAll(dot, statusTxt);

        // friend id
        Label idLbl = new Label("ID: " + friend.getId());
        idLbl.setStyle("""
            -fx-text-fill: #4a4a48;
            -fx-font-size: 11px;
            -fx-font-weight: 600;
        """);

        info.getChildren().addAll(name, eloBadge, statusRow, idLbl);
        zone.getChildren().addAll(avatarWrap, info);
        return zone;
    }

    private Label buildInitialsLabel() {
        Label lbl = new Label(initials(friend.getUsername()));
        lbl.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 26px;
            -fx-font-weight: 900;
        """);
        return lbl;
    }

    // ── Action button ─────────────────────────────────────────────────

    private VBox buildAction() {
        VBox wrap = new VBox(8);
        wrap.setPadding(new Insets(16, 20, 20, 20));

        switch (friend.getStatus()) {

            case InLobby -> {
                // Challenge to a game
                Button challengeBtn = buildPrimaryBtn(
                        "⚔  Challenge",
                        "#81b64c",
                        "#1a1a14"
                );
                challengeBtn.setOnAction(e -> {
                    dismiss();
                    client.challenge(friend.getId());
                });
                wrap.getChildren().add(challengeBtn);
            }

            case InGame -> {
                // Spectate their current game
                Button spectateBtn = buildPrimaryBtn(
                        "👁  Spectate game",
                        "#e6b84a",
                        "#1a1a14"
                );
                spectateBtn.setOnAction(e -> {
                    dismiss();
                    client.spectate(friend.getId());
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
            client.deleteFriend(friend.getId());
        });
        wrap.getChildren().add(removeBtn);

        return wrap;
    }

    private Button buildPrimaryBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-background-radius: 10;
            -fx-font-size: 14px;
            -fx-font-weight: 700;
            -fx-padding: 11 0;
            -fx-cursor: hand;
        """, bg, fg));
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e  -> btn.setOpacity(1.0));
        return btn;
    }

    private Button buildSecondaryBtn(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle("""
            -fx-background-color: transparent;
            -fx-text-fill: #444444;
            -fx-background-radius: 10;
            -fx-border-color: #252522;
            -fx-border-radius: 10;
            -fx-border-width: 1;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 0;
        """);
        return btn;
    }

    private Button buildDangerBtn(String text) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(dangerBtnStyle(false));
        btn.setOnMouseEntered(e -> btn.setStyle(dangerBtnStyle(true)));
        btn.setOnMouseExited(e  -> btn.setStyle(dangerBtnStyle(false)));
        return btn;
    }

    private String dangerBtnStyle(boolean hovered) {
        return hovered ? """
            -fx-background-color: rgba(224,100,90,0.12);
            -fx-text-fill: #e0645a;
            -fx-background-radius: 10;
            -fx-border-color: rgba(224,100,90,0.4);
            -fx-border-radius: 10;
            -fx-border-width: 1;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 0;
            -fx-cursor: hand;
        """ : """
            -fx-background-color: transparent;
            -fx-text-fill: #8a4a45;
            -fx-background-radius: 10;
            -fx-border-color: #252522;
            -fx-border-radius: 10;
            -fx-border-width: 1;
            -fx-font-size: 13px;
            -fx-font-weight: 600;
            -fx-padding: 10 0;
            -fx-cursor: hand;
        """;
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

    private String statusAccentColor() {
        return switch (friend.getStatus()) {
            case InLobby -> "#81b64c";
            case InGame  -> "#e6b84a";
            case Offline -> "#333333";
        };
    }

    private String statusLabel() {
        return switch (friend.getStatus()) {
            case InLobby -> "In lobby";
            case InGame  -> "In game";
            case Offline -> "Offline";
        };
    }

    private String closeBtnStyle(boolean hovered) {
        return hovered ? """
            -fx-background-color: #2a2a2a;
            -fx-text-fill: #ffffff;
            -fx-background-radius: 6;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-cursor: hand;
            -fx-padding: 0;
        """ : """
            -fx-background-color: #1e1e1b;
            -fx-text-fill: #555555;
            -fx-background-radius: 6;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 6;
            -fx-border-width: 1;
            -fx-font-size: 11px;
            -fx-cursor: hand;
            -fx-padding: 0;
        """;
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