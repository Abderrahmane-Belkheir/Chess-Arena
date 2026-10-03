
package org.Core.UI.LobbyScreens.Lobby;

import javafx.scene.layout.StackPane;
import org.Core.Auth.DTO.UserSession;
import org.Core.Config.GameEventPublisher;
import org.Core.Game.History.GameHistoryClient;
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
    private final GameHistoryClient gameHistoryClient;
    private final GameEventPublisher gameEventPublisher;

    public LobbyControllerStub(StackPane appRoot,ViewNavigator viewNavigator,MatchmakingHandler matchmakingHandler,
                                GameHistoryClient gameHistoryClient, GameEventPublisher gameEventPublisher) {
        this.appRoot = appRoot;
        this.viewNavigator=new ViewNavigator(appRoot);
        this.matchmakingHandler=matchmakingHandler;
        this.gameHistoryClient=gameHistoryClient;
        this.gameEventPublisher=gameEventPublisher;
    }

    @Override
    public StackPane start(UserSession userSession, FriendShipClient friendShipClient) {
        this.currentSession = userSession;
        if (lobbyView == null) {
            lobbyView = new LobbyView(this,friendShipClient,gameHistoryClient,gameEventPublisher);
        }
        lobbyView.setUser(
                userSession.getUsername(),
                userSession.getElo(),
                Avatar.initials(userSession.getUsername()),
                userSession.getAvatarUrl()
        );
        return lobbyView.getView();
    }

    @Override
    public void refreshUser(UserSession userSession) {
        this.currentSession = userSession;
        lobbyView.setUser(
                userSession.getUsername(),
                userSession.getElo(),
                Avatar.initials(userSession.getUsername()),
                userSession.getAvatarUrl()
        );
    }

    @Override
    public StackPane getView() {
        return lobbyView.getView();
    }

    @Override
    public void showGame(StackPane gameContent) {
        lobbyView.showGame(gameContent);
    }

    @Override
    public void clearGame() {
        lobbyView.clearGame();
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