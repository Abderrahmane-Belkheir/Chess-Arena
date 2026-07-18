
package org.Core.UI.LobbyScreens.Lobby;

import javafx.scene.layout.StackPane;
import org.Core.Auth.DTO.UserSession;
import org.Core.Social.FriendShipClient;
import org.Core.UI.Game.MatchmakingHandler;
import org.Core.UI.LobbyScreens.Friends.Avatar;
import org.Core.UI.LobbyScreens.Profile.ProfileCard;
import org.Core.UI.LobbyScreens.Profile.ProfileCardController;
import org.Core.UI.Shared.ViewNavigator;


public class LobbyControllerStub implements LobbyController, ProfileCardController {

    private final StackPane appRoot;
    private org.Core.UI.LobbyScreens.Lobby.LobbyView lobbyView;
    private UserSession currentSession;
    private final ViewNavigator viewNavigator;
    private final MatchmakingHandler matchmakingHandler;

    public LobbyControllerStub(StackPane appRoot,ViewNavigator viewNavigator,MatchmakingHandler matchmakingHandler) {
        this.appRoot = appRoot;
        this.viewNavigator=new ViewNavigator(appRoot);
        this.matchmakingHandler=matchmakingHandler;
    }

    @Override
    public StackPane start(UserSession userSession, FriendShipClient friendShipClient) {
        this.currentSession = userSession;
        lobbyView = new LobbyView(this,friendShipClient);
        lobbyView.setUser(
                userSession.getUsername(),
                userSession.getElo(),
                Avatar.initials(userSession.getUsername()),
                userSession.getAvatarUrl()
        );
        return lobbyView.getView();
    }

    @Override
    public void onProfileClicked() {
        StackPane overlay = lobbyView.getOverlay();
        ProfileCard card = new ProfileCard(currentSession, this, overlay);
        card.show();
    }

    @Override
    public StackPane getOverlay() {
        return lobbyView.getOverlay();
    }


    @Override
    public void onChangeAvatar() {
        System.out.println("[Profile] Change avatar clicked");
    }

    @Override
    public void onPlayClicked() {
        matchmakingHandler.startGameSearching(lobbyView.getView());
    }



    @Override
    public void onGameClicked(String gameId) {

    }


}