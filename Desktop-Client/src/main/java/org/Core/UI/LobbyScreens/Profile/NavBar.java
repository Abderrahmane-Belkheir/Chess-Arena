
package org.Core.UI.LobbyScreens.Profile;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import org.Core.UI.LobbyScreens.Friends.Avatar;
import org.Core.UI.LobbyScreens.Lobby.LobbyController;

/**
 * NavBar — top bar.
 *
 * Left  : crown icon + "Chess Arena" app name
 * Right : circular avatar initials | username + "Online" | ELO badge
 *
 * Visual styling lives in /css/lobby.css (scoped to the lobby root).
 */
public class NavBar {

    private final HBox root = new HBox();

    // kept as fields so LobbyView.setUser() can update them live
    private StackPane avatarPane;
    private final Label usernameLabel = new Label("magnus_jr");
    private final Label eloLabel     = new Label("1420");
    private final HBox profileChip = new HBox(8); // field so setUser can swap avatarPane in place

    public NavBar(LobbyController controller) {

        root.setPrefHeight(48);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(0, 14, 0, 14));
        root.getStyleClass().add("lobby-navbar");

        // ── Left: logo ────────────────────────────────────────────────
        HBox logo = new HBox(7);
        logo.setAlignment(Pos.CENTER_LEFT);

        Label crown = new Label("♛");
        crown.getStyleClass().add("lobby-crown");

        Label appName = new Label("Chess Arena");
        appName.getStyleClass().add("lobby-app-name");

        logo.getChildren().addAll(crown, appName);

        // ── Spacer ────────────────────────────────────────────────────
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // ── Right: profile chip ───────────────────────────────────────
        profileChip.setAlignment(Pos.CENTER);
        profileChip.setPadding(new Insets(5, 10, 5, 6));
        profileChip.getStyleClass().add("profile-chip");

        avatarPane = Avatar.build(null, "MA", "#7c5c3e", 32); // placeholder until setUser

        // Username + status
        VBox userInfo = new VBox(1);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        usernameLabel.getStyleClass().add("nav-username");

        Label statusLabel = new Label("Online");
        statusLabel.getStyleClass().add("nav-status-online");

        userInfo.getChildren().addAll(usernameLabel, statusLabel);

        // ELO badge
        HBox eloBadge = new HBox(3);
        eloBadge.setAlignment(Pos.CENTER);
        eloBadge.setPadding(new Insets(3, 8, 3, 8));
        eloBadge.getStyleClass().add("elo-badge");

        Label eloStar = new Label("★");
        eloStar.getStyleClass().add("elo-star");
        eloLabel.getStyleClass().add("elo-value");
        eloBadge.getChildren().addAll(eloStar, eloLabel);

        profileChip.getChildren().addAll(avatarPane, userInfo, eloBadge);
        profileChip.setOnMouseClicked(e -> controller.onProfileClicked());

        root.getChildren().addAll(logo, spacer, profileChip);
    }

    // ── public API ────────────────────────────────────────────────────

    public void setUser(String username, int elo, String initials, String avatarUrl, String bgColor) {
        usernameLabel.setText(username);
        eloLabel.setText(String.valueOf(elo));

        String shownInitials = initials.length() > 2
                ? initials.substring(0, 2).toUpperCase()
                : initials.toUpperCase();

        StackPane newAvatar = Avatar.build(avatarUrl, shownInitials, bgColor, 32);
        profileChip.getChildren().set(0, newAvatar); // avatarPane is always index 0
        avatarPane = newAvatar;
    }

    public HBox getView() { return root; }
}
