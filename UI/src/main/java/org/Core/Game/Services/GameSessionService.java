package org.Core.Game.Services;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import javafx.application.Platform;
import javafx.scene.layout.StackPane;
import org.Core.Auth.UserSessionManager;
import org.Core.Game.Events.GameFound;
import org.Core.Game.Events.GameMove;
import org.Core.UI.Game.GameView;
import org.Core.UI.Shared.ViewNavigator;
import java.io.IOException;


public class GameSessionService{

    private GameView gameView;
    private  StackPane gameRoot;
    private  final ViewNavigator viewNavigator;
    private final UserSessionManager userSessionManager;

    @Inject
    public GameSessionService(UserSessionManager userSessionManager, ViewNavigator viewNavigator){
        this.userSessionManager=userSessionManager;
        this.viewNavigator=viewNavigator;
    }


    @Subscribe
    public void onMatchFound(GameFound event){
        Platform.runLater(()-> {
            try {
                this.gameView = new GameView(event.getFen(),
                        userSessionManager.getUserSession(false), event.getOpponent());

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
            viewNavigator.transitionTo(gameView.getView());
        });
    }

    @Subscribe
    public void onMove(GameMove event){
        System.out.println(event);
    }
}



