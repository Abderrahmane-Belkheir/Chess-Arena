
package org.Core.UI.LobbyScreens;

import javafx.scene.layout.StackPane;
import org.Core.Auth.DTO.UserSession;
import org.Core.Social.FriendShipClient;

import java.util.ArrayList;

/**
 * LobbyControllerStub — drop-in no-op implementation of LobbyController.
 *
 * Use this during UI development so everything compiles and runs.
 * Replace each method body with your real application logic:
 *
 *   - onPlayClicked()    → start matchmaking flow
 *   - onProfileClicked() → open profile / settings screen
 *   - onFriendClicked()  → open challenge / chat dialog
 *   - onGameClicked()    → open board replay viewer
 *
 * Usage in your main App / Router:
 *
 *   LobbyController ctrl = new LobbyControllerStub(); // swap for real impl later
 *   LobbyView lobby = new LobbyView(ctrl);
 *   scene.setRoot(lobby.getView());
 */
public class LobbyControllerStub implements LobbyController, ProfileCardController {

    private final StackPane appRoot;
    private LobbyView lobbyView;
    private UserSession currentSession;

    public LobbyControllerStub(StackPane appRoot) {
        this.appRoot = appRoot;
    }

    @Override
    public StackPane start(UserSession userSession, FriendShipClient friendShipClient) {
        this.currentSession = userSession;
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

    // ── ProfileCardController ─────────────────────────────────────────

    @Override
    public void onChangeAvatar() {
        // TODO: open file picker, upload to server, update avatar URL
        System.out.println("[Profile] Change avatar clicked");
    }





    @Override
    public void onPlayClicked() {
        System.out.println("[LobbyController] Play clicked");
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