package org.Core.Game.Services;

import org.Core.Game.Events.GameActionRequest;
import org.Core.Game.Events.PlayerMove;
import org.Core.Game.Events.Spectate;
import org.Core.Realtime.GameRealtimeGatewayStub;

import java.util.function.Consumer;

public class GameActions{
        public final static Consumer<PlayerMove> onMove=(move)-> GameRealtimeGatewayStub.getSession().send("/app/game.move",move);
        public final static Consumer<String> onResign= (gameId)-> GameRealtimeGatewayStub.getSession().send("/app/game.resign",new GameActionRequest(gameId));
        public final static Consumer<String> onOfferDraw=(gameId)-> GameRealtimeGatewayStub.getSession().send("/app/game.draw.offer",new GameActionRequest(gameId));
        public final static Consumer<String> onAcceptDraw=(gameId)-> GameRealtimeGatewayStub.getSession().send("/app/game.draw.accept",new GameActionRequest(gameId));
        public final static Consumer<Integer> onAcceptSpectate=(playerId)-> GameRealtimeGatewayStub.getSession().send("/app/spectate.accept",new Spectate(playerId));
    }