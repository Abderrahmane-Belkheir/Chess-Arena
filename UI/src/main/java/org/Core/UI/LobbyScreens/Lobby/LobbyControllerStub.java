
package org.Core.UI.LobbyScreens.Lobby;

import javafx.scene.layout.StackPane;
import org.Core.Auth.DTO.UserSession;
import org.Core.Realtime.RealtimeGateway;
import org.Core.Social.FriendShipClient;
import org.Core.UI.LobbyScreens.Game.MatchmakingView;
import org.Core.UI.LobbyScreens.Profile.ProfileCard;
import org.Core.UI.LobbyScreens.Profile.ProfileCardController;
import org.Core.UI.Shared.ViewNavigator;


public class LobbyControllerStub implements LobbyController, ProfileCardController {

    private final StackPane appRoot;
    private org.Core.UI.LobbyScreens.Lobby.LobbyView lobbyView;
    private UserSession currentSession;
    private final RealtimeGateway websocket;
    private  FriendShipClient friendShipClient;
    private final ViewNavigator viewNavigator;

    public LobbyControllerStub(StackPane appRoot, RealtimeGateway websocket,ViewNavigator viewNavigator) {
        this.appRoot = appRoot;
        this.websocket=websocket;
        this.viewNavigator=new ViewNavigator(appRoot);
    }

    @Override
    public StackPane start(UserSession userSession, FriendShipClient friendShipClient) {
        this.currentSession = userSession;
        this.friendShipClient=friendShipClient;
        lobbyView = new LobbyView(this,friendShipClient);
        lobbyView.setUser(userSession.getUsername(), userSession.getElo(), userSession.getAvatarUrl());
        return lobbyView.getView();
    }

    @Override
    public void onProfileClicked() {
        StackPane overlay = lobbyView.getOverlay(); // we'll add this method below
        ProfileCard card = new ProfileCard(currentSession, this, overlay);
        card.show();
    }


    @Override
    public void onChangeAvatar() {
        // TODO: open file picker, upload to server, update avatar URL
        System.out.println("[Profile] Change avatar clicked");
    }





    @Override
    public void onPlayClicked() {
        MatchmakingView matchmaking = new MatchmakingView(() -> {
            websocket.stopGameSearching();
            viewNavigator.transitionTo(lobbyView.getView());
        });

        viewNavigator.transitionTo(matchmaking.getView());

        websocket.startGameSearching();
    }

    @Override
    public void onFriendClicked(String username) {
        System.out.println("[LobbyController] Friend clicked: " + username);
    }

    @Override
    public void onGameClicked(String gameId) {
        System.out.println("[LobbyController] Game clicked: " + gameId);
    }
}