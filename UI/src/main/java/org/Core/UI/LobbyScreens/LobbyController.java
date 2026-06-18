
package org.Core.UI.LobbyScreens;

import javafx.scene.layout.StackPane;
import org.Core.Auth.DTO.UserSession;

/**
 * LobbyController — every button / action in the lobby calls through here.
 *
 * Implement this interface in your application layer and pass it into
 * LobbyView.  The UI never touches game logic directly.
 *
 * TODO: implement each method with real backend / service calls.
 */
public interface LobbyController {

    StackPane start(UserSession userSession);
    // ── Hero ──────────────────────────────────────────────────────────

    /** User clicked the big green "Play" button. */
    void onPlayClicked();

    // ── Nav ───────────────────────────────────────────────────────────

    /** User clicked their own profile chip in the top-right. */
    void onProfileClicked();

    // ── Friends ───────────────────────────────────────────────────────

    /** User clicked a friend row — open challenge / chat dialog. */
    void onFriendClicked(String username);

    // ── Recent games ─────────────────────────────────────────────────

    /** User clicked a past game row — open board replay. */
    void onGameClicked(String gameId);
}