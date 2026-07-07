package org.Core.UI.LobbyScreens.Lobby;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.Core.Social.FriendShipClient;
import org.Core.UI.Game.RecentGames;
import org.Core.UI.LobbyScreens.Center.HeroPanel;
import org.Core.UI.LobbyScreens.Friends.Avatar;
import org.Core.UI.LobbyScreens.Friends.FriendsPanel;
import org.Core.UI.LobbyScreens.Profile.NavBar;


public class LobbyView {

    private final StackPane root = new StackPane();  // ← changed
    @Getter
    private final StackPane overlay = new StackPane();
    private final NavBar navBar;
    private final FriendsPanel friendsPanel;
    private final HeroPanel heroPanel;
    private final RecentGames recentGames;

    public LobbyView(LobbyController controller, FriendShipClient friendShipClient) {

        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #0a0a0a;");

        navBar       = new NavBar(controller);
        friendsPanel = new FriendsPanel(friendShipClient,controller);
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
        overlay.setVisible(false);
        overlay.setPickOnBounds(false);
        root.getChildren().addAll(layout,overlay);
    }

    public void setUser(String username, int elo, String avatarInitials, String avatarUrl) {
        navBar.setUser(username, elo, avatarInitials, avatarUrl, Avatar.colorFromName(username));
    }

    public void setRecentGames(java.util.List<RecentGames.GameEntry> games) {
        recentGames.setGames(games);
    }

    public StackPane getView() { return root; }  // ← now returns StackPane
}