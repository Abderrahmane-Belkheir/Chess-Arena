package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Services.Game.Events.*;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.GameSession;
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

    public void resign(String playerInternalId,String gameId){
        GameSession session=authorizationService.AuthorizePlayer(playerInternalId,gameId);
        // here its clear that the opponent is the winner so we pass in the handle method which is expecting the winner
        String opponentInternalId = Objects.equals(session.getWhitePlayerId(), playerInternalId) ?session.getBlackPlayerId():session.getWhitePlayerId();
        Color opponentColor=Objects.equals(session.getWhitePlayerId(), opponentInternalId) ?Color.WHITE:Color.BLACK;
       gameOverHandler.handle(gameId,playerInternalId, opponentInternalId,opponentColor, GameOverInfo.EndReason.RESIGNATION);
        GameOverInfo winner=new GameOverInfo(GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.RESIGNATION);
        GameOverInfo loser =new GameOverInfo(GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.RESIGNATION);
        timeOutSchedulingService.cancel(gameId);
        int playerPublicId =opponentColor==Color.WHITE?session.getBlackPlayerPublicId():session.getWhitePlayerPublicId();
        int OpponentPublicId=opponentColor==Color.WHITE?session.getWhitePlayerPublicId():session.getBlackPlayerPublicId();
        Id opponentId=new Id(opponentInternalId,OpponentPublicId);
        Id playerId=new Id(playerInternalId,playerPublicId);
        eventPublisher.publishEvent(new GameOverEvent(new Event(playerId,loser),new Event(opponentId,winner)));
    }


    public void offerDraw(String userId,String gameId){
        GameSession session=authorizationService.AuthorizePlayer(userId,gameId);
        Color playerColor=Objects.equals(session.getWhitePlayerId(),userId)?Color.WHITE:Color.BLACK;
        if(session.getTurn()==playerColor) return;
        if(!drawOfferStore.offerDraw(gameId,playerColor)) return;
        String opponentInternalId=Objects.equals(session.getWhitePlayerId(),userId)?session.getBlackPlayerId():session.getWhitePlayerId();
        eventPublisher.publishEvent(new  Event(new Id(opponentInternalId,0),new DrawOfferEvent(
                opponentInternalId
        )));
    }


    public void acceptDraw(String playerInternalId, String gameId){
       GameSession session=authorizationService.AuthorizePlayer(playerInternalId,gameId);
        DrawOffer offer=drawOfferStore.get(gameId).orElseThrow();
        String opponentInternalId =Objects.equals(session.getWhitePlayerId(), playerInternalId)?session.getBlackPlayerId():session.getWhitePlayerId();
        drawOfferStore.clear(gameId);
        gameOverHandler.handle(gameId, playerInternalId, opponentInternalId,Color.NONE, GameOverInfo.EndReason.DRAW_AGREEMENT);
        timeOutSchedulingService.cancel(gameId);
        GameOverInfo player=new GameOverInfo(GameOverInfo.GameResult.DRAW, GameOverInfo.EndReason.DRAW_AGREEMENT);
        GameOverInfo opponent=new GameOverInfo(GameOverInfo.GameResult.DRAW, GameOverInfo.EndReason.DRAW_AGREEMENT);
        int playerPublicId =playerInternalId.equals(session.getWhitePlayerId())?session.getWhitePlayerPublicId():session.getBlackPlayerPublicId();
        int OpponentPublicId= playerInternalId.equals(session.getWhitePlayerId())?session.getBlackPlayerPublicId():session.getWhitePlayerPublicId();
        Id opponentId=new Id(opponentInternalId,OpponentPublicId);
        Id playerId=new Id(playerInternalId,playerPublicId);
        eventPublisher.publishEvent(new GameOverEvent(new Event(playerId,player),new Event(opponentId,opponent)));
    }

}
