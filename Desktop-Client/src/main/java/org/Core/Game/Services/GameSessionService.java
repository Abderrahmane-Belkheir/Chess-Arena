package org.Core.Game.Services;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.util.Duration;
import lombok.Getter;
import org.Core.Auth.UserSessionManager;
import org.Core.Game.Events.*;
import org.Core.Realtime.RealtimeGateway;
import org.Core.UI.Game.GameJoinOverlay;
import org.Core.UI.Game.GameView;
import org.Core.UI.OpeningScreens.GameController;
import org.Core.UI.Shared.ViewNavigator;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;


public class GameSessionService{

    private static final int RESIGN_ON_CLOSE_TIMEOUT_SECONDS = 5;

    private GameView gameView;
    private String currentGameId;
    // Claimed (getAndSet(null)) by whichever fires first: the real game-over
    // event, or the timeout fallback in resignForAppClose — so a dropped
    // connection can't leave the app stuck unable to close, and a late
    // response after the timeout can't double-run the close.
    private final AtomicReference<Runnable> pendingCloseAfterGameOver = new AtomicReference<>();
    @Getter
    private  final ViewNavigator viewNavigator;
    private final UserSessionManager userSessionManager;
    private final GameController gameController;
    private final RealtimeGateway realtimeGateway;

    @Inject
    public GameSessionService(UserSessionManager userSessionManager, ViewNavigator viewNavigator, GameController gameController, RealtimeGateway realtimeGateway){
        this.userSessionManager=userSessionManager;
        this.viewNavigator=viewNavigator;
        this.gameController=gameController;
        this.realtimeGateway=realtimeGateway;
    }


    @Subscribe
    public void onMatchFound(GameFound event){

        Platform.runLater(()-> {
            // GameFound is now session-long-subscribed (challenge-accepted
            // games can arrive any time, not just while searching), so a
            // second one — e.g. a stale challenge someone accepted while
            // you're already elsewhere — must not clobber whatever you're
            // already doing instead of just replacing it silently.
            if (this.gameView != null) {
                return;
            }

            GameJoinOverlay hookOverlay = new GameJoinOverlay(
                    "Match found! Setting up your game against " + event.getOpponent().getUsername() + "…");
            viewNavigator.transitionTo(hookOverlay.getView());

            try {
                this.gameView = new GameView(event.getId(),event.getFen(),
                        userSessionManager.getUserSession(false),
                        event.getMySide(),event.getOpponent(),newGame(),returnToLobby());
                this.currentGameId = event.getId();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            hookOverlay.playThenRun(() -> gameController.showGame(gameView.getView()));
        });
    }


    public Runnable returnToLobby(){
        return () -> {
            if (gameView != null) {
                gameView.stopClocks();
                // Spectator's /topic/spectate/{id} feed keeps delivering moves
                // for the game we just left otherwise — with gameView about to
                // go null, the next one would NPE in onOpponentMove/onConfirmMove.
                if (gameView.isSpectatorMode()) realtimeGateway.stopSpectating();
            }
            gameView = null;
            currentGameId = null;
            gameController.exitGame();
        };
    }

    public Runnable newGame(){
        return () -> {
            if (gameView != null) gameView.stopClocks();
            gameView = null;
            currentGameId = null;
            gameController.startNewGame();
        };
    }

    /** True only for an actual in-progress game you're playing — not spectating, and not one that's already ended. */
    public boolean isInActiveGame(){
        return gameView != null && !gameView.isSpectatorMode() && !gameView.isGameOver();
    }

    /**
     * Sends the same resign action the in-board Resign button sends, then
     * runs afterShown once the resulting game-over card has actually been
     * built (queued behind it via Platform.runLater in onGameOver) — or
     * after a short timeout if the server never responds, so closing the
     * app can't get stuck waiting forever.
     */
    public void resignForAppClose(Runnable afterShown){
        if (!isInActiveGame() || currentGameId == null) {
            afterShown.run();
            return;
        }

        pendingCloseAfterGameOver.set(afterShown);
        gameView.markClosingApp(); // the app is closing regardless of the result, so the "New Game"/"Lobby" buttons on the card that's about to appear shouldn't be usable
        GameActions.onResign.accept(currentGameId);

        PauseTransition timeout = new PauseTransition(Duration.seconds(RESIGN_ON_CLOSE_TIMEOUT_SECONDS));
        timeout.setOnFinished(e -> runPendingClose());
        timeout.play();
    }

    private void runPendingClose(){
        Runnable after = pendingCloseAfterGameOver.getAndSet(null);
        if (after != null) after.run();
    }

    @Subscribe
    public void onOpponentMove(OpponentMove move){
        gameView.applyOpponentMove(move);
    }

    @Subscribe
    public void onGameOver(GameOverInfo gameOverInfo){
        gameView.gameOver(gameOverInfo);
        // showGameOverCard (inside gameView.gameOver) already queued its own
        // Platform.runLater to build the card; queuing this one right after,
        // from the same calling thread, lands behind it in the FX queue — so
        // the card is guaranteed to exist before a pending close proceeds.
        // A no-op when nothing is pending, so safe to call unconditionally.
        Platform.runLater(this::runPendingClose);
    }

    @Subscribe
    public void onConfirmMove(MoveConfirmation moveConfirmation){
        gameView.applyMoveConfirmation(moveConfirmation);
    }

    @Subscribe
    public void onDrawOffered(DrawOfferReceived drawOfferReceived){
        gameView.showDrawOffered();
    }


    @Subscribe
    public void onSpectateRequested(SpectatedResponse response){
        System.out.println(response);
        gameView.showSpectateRequest(response);
    }

    @Subscribe
    public void onSpectateAccepted(SpectatorResponse response){
        if (this.gameView != null) {
            return;
        }

        Platform.runLater(()-> {

            GameJoinOverlay hookOverlay = new GameJoinOverlay(
                    "Hooking into " + response.getSpectatedPlayer().getUsername() + "'s game…");
            viewNavigator.transitionTo(hookOverlay.getView());

            this.gameView = new GameView(response.getFen(), response.getSpectatedSide(),
                    response.getSpectatedPlayer(), response.getOpponent(), response.getSpectatedTimeMs(),
                    response.getOtherTimeMs(), response.getTurn(), returnToLobby());

            hookOverlay.playThenRun(() -> gameController.showGame(gameView.getView()));
        });
    }




}



