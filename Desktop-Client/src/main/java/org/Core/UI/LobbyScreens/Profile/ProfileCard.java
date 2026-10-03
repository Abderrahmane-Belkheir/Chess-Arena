package org.Core.UI.LobbyScreens.Profile;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import org.Core.Auth.DTO.UserSession;

/**
 * The player's own profile card — same compact layout and palette as
 * FriendCard (avatar with glow + status ring, name, elo badge, status row,
 * id label), just without any of the friend-interaction action buttons at
 * the bottom, since challenge/spectate/remove don't apply to your own
 * profile. Reuses FriendCard's "modal-*"/"status-*-inlobby" CSS classes so
 * it matches the rest of the app's warm wood/gold theme instead of the
 * old, much taller, differently-themed layout this replaced.
 */
public class ProfileCard {

    private final StackPane overlay;
    private final VBox card;
    private final UserSession session;
    private final ProfileCardController controller;

    public ProfileCard(UserSession session,
                       ProfileCardController controller,
                       StackPane overlay) {
        this.session    = session;
        this.controller = controller;
        this.overlay    = overlay;

        overlay.getStyleClass().add("modal-overlay");
        overlay.setOnMouseClicked(e -> { if (e.getTarget() == overlay) dismiss(); });
        overlay.setAlignment(Pos.CENTER);

        card = new VBox(0);
        card.setPrefWidth(404);
        card.setMaxWidth(404);
        // StackPane stretches children to fill it unless maxHeight is capped —
        // lock it back to its content height (just the header + avatar zone).
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.getStyleClass().add("friend-card-modal");
        card.setOnMouseClicked(e -> e.consume());

        card.getChildren().addAll(
                buildHeader(),
                buildAvatarZone()
        );

        overlay.getChildren().add(card);
    }

    // ── Header ────────────────────────────────────────────────────────

    private HBox buildHeader() {
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(18, 22, 0, 22));

        Label label = new Label("MY PROFILE");
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

        StackPane avatarGlowWrap = new StackPane();
        avatarGlowWrap.setPrefSize(148, 148);
        avatarGlowWrap.setMinSize(148, 148);
        avatarGlowWrap.setMaxSize(148, 148);
        Region glow = new Region();
        glow.setPrefSize(140, 140);
        glow.getStyleClass().add("modal-avatar-glow");

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
        """, avatarColor(session.getUsername())));

        if (session.getAvatarUrl() != null && !session.getAvatarUrl().isEmpty()) {
            try {
                Image img = new Image(session.getAvatarUrl(), 94, 94, true, true, true);
                ImageView iv = new ImageView(img);
                iv.setFitWidth(94);
                iv.setFitHeight(94);
                Circle clip = new Circle(47, 47, 47);
                iv.setClip(clip);
                avatarWrap.getChildren().addAll(circle, iv);
            } catch (Exception ignored) {
                avatarWrap.getChildren().addAll(circle, buildInitialsLabel());
            }
        } else {
            avatarWrap.getChildren().addAll(circle, buildInitialsLabel());
        }

        // Your own profile is only ever shown while you're using the app —
        // always "online", so reuse the InLobby ring/dot/text styling as a
        // fixed state rather than switching on it.
        Region ring = new Region();
        ring.setPrefSize(100, 100);
        ring.setMinSize(100, 100);
        ring.setMaxSize(100, 100);
        ring.getStyleClass().add("status-ring-inlobby");
        avatarWrap.getChildren().add(ring);

        StackPane editBadge = new StackPane();
        editBadge.setPrefSize(26, 26);
        editBadge.setMinSize(26, 26);
        editBadge.setMaxSize(26, 26);
        editBadge.setStyle("""
            -fx-background-color: #e8cf8a;
            -fx-background-radius: 13;
            -fx-border-color: #201a15;
            -fx-border-radius: 13;
            -fx-border-width: 2;
            -fx-cursor: hand;
        """);
        Label pencil = new Label("✎");
        pencil.setStyle("-fx-text-fill: #1a1611; -fx-font-size: 11px;");
        editBadge.getChildren().add(pencil);
        StackPane.setAlignment(editBadge, Pos.BOTTOM_RIGHT);
        editBadge.setOnMouseClicked(e -> controller.onChangeAvatar());
        avatarWrap.getChildren().add(editBadge);

        avatarGlowWrap.getChildren().addAll(glow, avatarWrap);

        // info
        VBox info = new VBox(6);
        info.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label name = new Label(session.getUsername());
        name.getStyleClass().add("modal-username");

        HBox eloBadge = new HBox(5);
        eloBadge.setAlignment(Pos.CENTER);
        eloBadge.setPadding(new Insets(3, 9, 3, 9));
        eloBadge.getStyleClass().add("modal-elo-badge");
        Label star = new Label("★");
        star.getStyleClass().add("modal-elo-star");
        Label eloVal = new Label(String.valueOf(session.getElo()));
        eloVal.getStyleClass().add("modal-elo-value");
        eloBadge.getChildren().addAll(star, eloVal);

        HBox statusRow = new HBox(5);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setPrefSize(7, 7);
        dot.getStyleClass().add("status-dotshape-inlobby");
        Label statusTxt = new Label("Online");
        statusTxt.getStyleClass().addAll("modal-status-text", "status-accent-inlobby");
        statusRow.getChildren().addAll(dot, statusTxt);

        Label idLbl = new Label("ID: " + session.getId());
        idLbl.getStyleClass().add("modal-id-text");

        info.getChildren().addAll(name, eloBadge, statusRow, idLbl);
        zone.getChildren().addAll(avatarGlowWrap, info);
        return zone;
    }

    private Label buildInitialsLabel() {
        Label lbl = new Label(initials(session.getUsername()));
        lbl.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 26px; -fx-font-weight: 900;");
        return lbl;
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
