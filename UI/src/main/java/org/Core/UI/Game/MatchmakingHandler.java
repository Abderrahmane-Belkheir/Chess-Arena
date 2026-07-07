package org.Core.UI.Game;

import com.google.inject.Inject;
import javafx.scene.layout.StackPane;
import org.Core.Realtime.RealtimeGateway;
import org.Core.UI.Shared.ViewNavigator;

public class MatchmakingHandler {

    private  final RealtimeGateway realtimeGateway;
    private final ViewNavigator viewNavigator;

    @Inject
    public MatchmakingHandler(RealtimeGateway realtimeGateway, ViewNavigator viewNavigator){
        this.realtimeGateway=realtimeGateway;
        this.viewNavigator=viewNavigator;
    }

    public  void startGameSearching(StackPane pane) {
        MatchmakingView matchmaking = new MatchmakingView(() -> {
            realtimeGateway.stopGameSearching();
            viewNavigator.transitionTo(pane);
        });

        viewNavigator.transitionTo(matchmaking.getView());

        realtimeGateway.startGameSearching();
    }
}
