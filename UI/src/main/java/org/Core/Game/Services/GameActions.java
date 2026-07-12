package org.Core.Game.Services;

import org.Core.Game.Events.GameActionRequest;
import org.Core.Game.Events.PlayerMove;
import org.Core.Realtime.RealtimeGatewayStub;

import java.util.function.Consumer;

public class GameActions{
        public final static Consumer<PlayerMove> onMove=(move)-> RealtimeGatewayStub.getSession().send("/app/game.move",move);
        public final static Consumer<String> onResign= (gameId)->RealtimeGatewayStub.getSession().send("/app/game.resign",new GameActionRequest(gameId));
        public final static Consumer<String> onOfferDraw=(gameId)->RealtimeGatewayStub.getSession().send("/app/game.draw.offer",new GameActionRequest(gameId));
        public final static Consumer<String> onAcceptDraw=(gameId)->RealtimeGatewayStub.getSession().send("/app/game.draw.accept",new GameActionRequest(gameId));
    }