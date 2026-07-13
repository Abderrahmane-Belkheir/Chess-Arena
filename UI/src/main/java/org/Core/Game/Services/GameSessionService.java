package org.Core.Game.Services;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import lombok.Getter;
import org.Core.Auth.UserSessionManager;
import org.Core.Game.Events.*;
import org.Core.Realtime.RealtimeGatewayStub;
import org.Core.UI.Game.GameView;
import org.Core.UI.Game.MatchmakingHandler;
import org.Core.UI.OpeningScreens.GameController;
import org.Core.UI.Shared.ViewNavigator;


import java.io.IOException;


public class GameSessionService{

    private GameView gameView;
    @Getter
    private  final ViewNavigator viewNavigator;
    private final UserSessionManager userSessionManager;
    private final MatchmakingHandler matchmakingHandler;
    private final GameController gameController;

    @Inject
    public GameSessionService(UserSessionManager userSessionManager, ViewNavigator viewNavigator, MatchmakingHandler matchmakingHandler, GameController gameController){
        this.userSessionManager=userSessionManager;
        this.viewNavigator=viewNavigator;
        this.matchmakingHandler=matchmakingHandler;
        this.gameController=gameController;
    }


    @Subscribe
    public void onMatchFound(GameFound event){
        Platform.runLater(()-> {
            try {
                this.gameView = new GameView(false,event.getId(),event.getFen(),
                        userSessionManager.getUserSession(false),
                        event.getMySide(),event.getOpponent(),matchmakingHandler,returnToLobby());

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            viewNavigator.transitionTo(gameView.getView());
        });
    }


    public Runnable returnToLobby(){
        return gameController::transitionToLobby;
    }

    @Subscribe
    public void onOpponentMove(OpponentMove move){
            gameView.applyOpponentMove(move);
    }

    @Subscribe
    public void onGameOver(GameOverInfo gameOverInfo){
        gameView.gameOver(gameOverInfo);
    }

    @Subscribe
    public void onConfirmMove(MoveConfirmation moveConfirmation){
        gameView.applyMoveConfirmation(moveConfirmation);
    }

    @Subscribe
    public void onDrawOffered(DrawOfferReceived drawOfferReceived){
        gameView.showDrawOffered();
    }



}



