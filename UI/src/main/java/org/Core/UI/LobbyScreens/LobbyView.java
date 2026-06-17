package org.Core.UI.LobbyScreens;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.*;
import javafx.scene.control.*;

/**
 * LobbyView — main screen after login.
 *
 * Layout (mirrors the screenshot):
 * ┌─────────────────────────────────────────────────────┐
 * │  NavBar  (logo left | profile + elo right)          │
 * ├──────────┬──────────────────────────┬───────────────┤
 * │ Friends  │      HeroPanel           │ Recent Games  │
 * │ sidebar  │  (chess bg + Play btn)   │ sidebar       │
 * └──────────┴──────────────────────────┴───────────────┘
 *
 * Inject real logic via the LobbyController interface.
 */
public class LobbyView {

    private final StackPane root = new StackPane();  // ← changed

    private final NavBar        navBar;
    private final FriendsPanel  friendsPanel;
    private final HeroPanel     heroPanel;
    private final RecentGames   recentGames;

    public LobbyView(LobbyController controller) {

        BorderPane layout = new BorderPane();  // ← layout stays BorderPane internally
        layout.setStyle("-fx-background-color: #0a0a0a;");

        navBar       = new NavBar(controller);
        friendsPanel = new FriendsPanel(controller);
        heroPanel    = new HeroPanel(controller);
        recentGames  = new RecentGames(controller);

        layout.setTop(navBar.getView());

        HBox body = new HBox(0);
        body.setFillHeight(true);
        HBox.setHgrow(heroPanel.getView(), Priority.ALWAYS);
        body.getChildren().addAll(
                friendsPanel.getView(),
                heroPanel.getView(),
                recentGames.getView()
        );

        layout.setCenter(body);
        root.getChildren().add(layout);  // ← wrap it
    }

    public void setUser(String username, int elo, String avatarInitials) {
        navBar.setUser(username, elo, avatarInitials);
    }

    public void setFriends(java.util.List<FriendsPanel.FriendEntry> friends) {
        friendsPanel.setFriends(friends);
    }

    public void setRecentGames(java.util.List<RecentGames.GameEntry> games) {
        recentGames.setGames(games);
    }

    public StackPane getView() { return root; }  // ← now returns StackPane
}