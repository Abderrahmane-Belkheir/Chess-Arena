package org.Core.Game.Services;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import org.Core.Auth.UserSessionManager;
import org.Core.Game.Events.*;
import org.Core.Realtime.RealtimeGatewayStub;
import org.Core.UI.Game.GameView;
import org.Core.UI.Game.MatchmakingHandler;
import org.Core.UI.OpeningScreens.GameController;
import org.Core.UI.OpeningScreens.GameControllerStub;
import org.Core.UI.Shared.ViewNavigator;
import org.springframework.messaging.simp.stomp.StompSession;


import java.io.IOException;
import java.util.function.Consumer;


public class GameSessionService{

    private GameView gameView;
    @Getter
    private  final ViewNavigator viewNavigator;
    private final UserSessionManager userSessionManager;
    private final MatchmakingHandler matchmakingHandler;
    private final GameController gameController;
    private final StompSession stompSession=RealtimeGatewayStub.getSession();

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
                this.gameView = new GameView(event.getId(),event.getFen(),
                        userSessionManager.getUserSession(false),
                        event.getMySide(),event.getOpponent(),matchmakingHandler,returnToLobby(),sendPlayerMove(),sendResign());

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            viewNavigator.transitionTo(gameView.getView());
        });
    }


    public Runnable returnToLobby(){
        return gameController::transitionToLobby;
    }

    public Consumer<PlayerMove> sendPlayerMove() {
        return (move)-> RealtimeGatewayStub.getSession().send("/app/game.move",move);
    }

    public Consumer<String> sendResign(){
        return (gameId)->RealtimeGatewayStub.getSession().send("/app/game.resign",new ResignRequest(gameId));
    }

    @Subscribe
    public void onOpponentMove(OpponentMove event){
            gameView.applyOpponentMove(event);
    }

    @Subscribe
    public void onGameOver(GameOverInfo gameOverInfo){
        gameView.gameOver(gameOverInfo);
    }

    @Subscribe
    public void onConfirmMove(MoveConfirmation moveConfirmation){
        gameView.applyMoveConfirmation(moveConfirmation);
    }
}



