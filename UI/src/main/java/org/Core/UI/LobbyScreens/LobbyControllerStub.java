
package org.Core.UI.LobbyScreens;

import javafx.scene.layout.StackPane;
import org.Core.Auth.UserSession;

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
public class LobbyControllerStub implements LobbyController {

    private final StackPane appRoot; // reference to main root to swap views

    public LobbyControllerStub(StackPane appRoot) {
        this.appRoot = appRoot;
    }
    @Override
    public StackPane start(UserSession userSession) {
        LobbyView lobby = new LobbyView(this);
        lobby.setUser(userSession.getUsername(), userSession.getElo(), userSession.getAvatarUrl());
        return lobby.getView();
    }

    @Override
    public void onPlayClicked() {
        System.out.println("[LobbyController] Play clicked");
    }

    @Override
    public void onProfileClicked() {

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