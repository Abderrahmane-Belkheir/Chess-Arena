package org.Core.UI.LobbyScreens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

/**
 * NavBar — top bar.
 *
 * Left  : crown icon + "Chess Arena" app name
 * Right : circular avatar initials | username + "Online" | ELO badge
 *
 * Screenshot reference: dark #111111 bar, 1px bottom border #1e1e1e,
 * avatar is a filled circle with initials, ELO shown in a separate
 * pill/badge to the right of the username block.
 */
public class NavBar {

    private final HBox root = new HBox();

    // kept as fields so LobbyView.setUser() can update them live
    private final Label avatarLabel  = new Label("MA");
    private final Label usernameLabel = new Label("magnus_jr");
    private final Label eloLabel     = new Label("1420");

    public NavBar(LobbyController controller) {

        root.setPrefHeight(64);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(0, 20, 0, 20));
        root.setStyle("""
            -fx-background-color: #111111;
            -fx-border-color: transparent transparent #1e1e1e transparent;
            -fx-border-width: 1;
        """);

        // ── Left: logo ────────────────────────────────────────────────
        HBox logo = new HBox(10);
        logo.setAlignment(Pos.CENTER_LEFT);

        Label crown = new Label("♛");
        crown.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 20px;");

        Label appName = new Label("Chess Arena");
        appName.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 16px;
            -fx-font-weight: 800;
        """);

        logo.getChildren().addAll(crown, appName);

        // ── Spacer ────────────────────────────────────────────────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Right: profile chip ───────────────────────────────────────
        HBox profileChip = new HBox(12);
        profileChip.setAlignment(Pos.CENTER);
        profileChip.setPadding(new Insets(8, 14, 8, 10));
        profileChip.setStyle("""
            -fx-background-color: #1e1e1e;
            -fx-background-radius: 10;
            -fx-border-color: #2a2a2a;
            -fx-border-radius: 10;
            -fx-border-width: 1;
            -fx-cursor: hand;
        """);

        // Avatar circle with initials
        StackPane avatar = buildAvatar(avatarLabel, "#7c5c3e");

        // Username + status
        VBox userInfo = new VBox(2);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        usernameLabel.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 13px;
            -fx-font-weight: 700;
        """);

        Label statusLabel = new Label("Online");
        statusLabel.setStyle("-fx-text-fill: #81b64c; -fx-font-size: 11px;");

        userInfo.getChildren().addAll(usernameLabel, statusLabel);

        // ELO badge
        HBox eloBadge = new HBox();
        eloBadge.setAlignment(Pos.CENTER);
        eloBadge.setPadding(new Insets(4, 10, 4, 10));
        eloBadge.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-background-radius: 6;
        """);
        eloLabel.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 13px;
            -fx-font-weight: 800;
        """);
        eloBadge.getChildren().add(eloLabel);

        profileChip.getChildren().addAll(avatar, userInfo, eloBadge);
        profileChip.setOnMouseClicked(e -> controller.onProfileClicked());
        profileChip.setOnMouseEntered(e -> profileChip.setStyle(profileChip.getStyle()
                .replace("#1e1e1e", "#252525")));
        profileChip.setOnMouseExited(e -> profileChip.setStyle(profileChip.getStyle()
                .replace("#252525", "#1e1e1e")));

        root.getChildren().addAll(logo, spacer, profileChip);
    }

    // ── public API ────────────────────────────────────────────────────

    public void setUser(String username, int elo, String initials) {
        usernameLabel.setText(username);
        eloLabel.setText(String.valueOf(elo));
        avatarLabel.setText(initials.length() > 2
                ? initials.substring(0, 2).toUpperCase()
                : initials.toUpperCase());
    }

    public HBox getView() { return root; }

    // ── helpers ───────────────────────────────────────────────────────

    static StackPane buildAvatar(Label initials, String bgColor) {
        StackPane pane = new StackPane();
        pane.setPrefSize(36, 36);
        pane.setMinSize(36, 36);
        pane.setMaxSize(36, 36);
        pane.setStyle(String.format("""
            -fx-background-color: %s;
            -fx-background-radius: 18;
        """, bgColor));
        initials.setStyle("""
            -fx-text-fill: #ffffff;
            -fx-font-size: 12px;
            -fx-font-weight: 700;
        """);
        pane.getChildren().add(initials);
        return pane;
    }
}