package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Services.Game.Events.GameOverInfo;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Services.Game.Events.DrawOfferEvent;
import org.Core.GameLogic.Services.Game.Events.GameOverEvent;
import org.Core.Scheduling.TimeOutSchedulingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GameActionsService {

    private final GameOverHandler gameOverHandler;
    private final GameAuthorizationService authorizationService;
    private final DrawOfferStore drawOfferStore;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeOutSchedulingService timeOutSchedulingService;

    public void resign(String userId,String gameId){
        GameSession session=authorizationService.AuthorizePlayer(userId,gameId);
        // here its clear that the opponent is the winner so we pass in the handle method which is expecting the winner
        String opponentId= Objects.equals(session.getWhitePlayerId(), userId) ?session.getBlackPlayerId():session.getWhitePlayerId();
        Color opponentColor=Objects.equals(session.getWhitePlayerId(),opponentId) ?Color.WHITE:Color.BLACK;
       gameOverHandler.handle(gameId,opponentColor, GameOverInfo.EndReason.RESIGNATION);
        GameOverInfo winner=new GameOverInfo(opponentId,GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.RESIGNATION);
        GameOverInfo loser =new GameOverInfo(userId,GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.RESIGNATION);
        timeOutSchedulingService.cancel(gameId);
        eventPublisher.publishEvent(new GameOverEvent(loser,winner));
    }


    public void offerDraw(String userId,String gameId){
        GameSession session=authorizationService.AuthorizePlayer(userId,gameId);
        Color playerColor=Objects.equals(session.getWhitePlayerId(),userId)?Color.WHITE:Color.BLACK;
        if(session.getTurn()==playerColor) return;
        if(!drawOfferStore.offerDraw(gameId,playerColor)) return;

        eventPublisher.publishEvent(new DrawOfferEvent(
                Objects.equals(session.getWhitePlayerId(),userId)?session.getBlackPlayerId():session.getWhitePlayerId()
        ));
    }


    public void acceptDraw(String userId,String gameId){
       GameSession session=authorizationService.AuthorizePlayer(userId,gameId);
        DrawOffer offer=drawOfferStore.get(gameId).orElseThrow();
        drawOfferStore.clear(gameId);
        gameOverHandler.handle(gameId,Color.NONE, GameOverInfo.EndReason.DRAW_AGREEMENT);
        timeOutSchedulingService.cancel(gameId);
        String opponentId =Objects.equals(session.getWhitePlayerId(),userId)?session.getBlackPlayerId():session.getWhitePlayerId();
        GameOverInfo player=new GameOverInfo(userId,GameOverInfo.GameResult.DRAW, GameOverInfo.EndReason.DRAW_AGREEMENT);
        GameOverInfo opponent=new GameOverInfo(opponentId,GameOverInfo.GameResult.DRAW, GameOverInfo.EndReason.DRAW_AGREEMENT);
        eventPublisher.publishEvent(new GameOverEvent(player,opponent));
    }

}
