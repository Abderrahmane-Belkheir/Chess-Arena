
package org.Core.UI.LobbyScreens.Lobby;

import javafx.scene.layout.StackPane;
import org.Core.Auth.DTO.UserSession;
import org.Core.Social.FriendShipClient;

/**
 * LobbyController — every button / action in the lobby calls through here.
 *
 * Implement this interface in your application layer and pass it into
 * LobbyView.  The UI never touches game logic directly.
 *
 * TODO: implement each method with real backend / service calls.
 */
public interface LobbyController {

    StackPane start(UserSession userSession, FriendShipClient friendShipClient);

    /** Re-applies userSession's current fields (e.g. elo, after a game changed it) to the already-built lobby view. */
    void refreshUser(UserSession userSession);

    void onPlayClicked();


    void onProfileClicked();


    StackPane getOverlay();
    void onGameClicked(String gameId);

    StackPane getView();
    void showGame(StackPane gameContent);
    void clearGame();
}