package org.Core.Game.Services;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import lombok.Getter;
import org.Core.Auth.UserSessionManager;
import org.Core.Game.Events.GameFound;
import org.Core.Game.Events.GameOverInfo;
import org.Core.Game.Events.OpponentMove;
import org.Core.Game.Events.PlayerMove;
import org.Core.Realtime.RealtimeGateway;
import org.Core.UI.Game.GameView;
import org.Core.UI.Shared.ViewNavigator;
import tools.jackson.databind.ObjectMapper;


import java.io.IOException;


public class GameSessionService{

    private GameView gameView;
    @Getter
    private  final ViewNavigator viewNavigator;
    private final UserSessionManager userSessionManager;
    private final ObjectMapper mapper;


    @Inject
    public GameSessionService(UserSessionManager userSessionManager, ViewNavigator viewNavigator, ObjectMapper mapper){
        this.userSessionManager=userSessionManager;
        this.viewNavigator=viewNavigator;
        this.mapper=mapper;
    }


    @Subscribe
    public void onMatchFound(GameFound event){
        Platform.runLater(()-> {
            try {
                this.gameView = new GameView(event.getId(),event.getFen(),
                        userSessionManager.getUserSession(false), event.getMySide(),event.getOpponent(),this);

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            viewNavigator.transitionTo(gameView.getView());
        });
    }

    @Subscribe
    public void onOpponentMove(OpponentMove event){
            gameView.applyOpponentMove(event);
    }

    public void sendPlayerMove(PlayerMove move) {
        RealtimeGateway.getSession().send("/app/game.move",move);
    }

    @Subscribe
    public void onGameOver(GameOverInfo gameOverInfo){
        gameView.showGameOverCard(gameOverInfo,null,null);
    }


}



