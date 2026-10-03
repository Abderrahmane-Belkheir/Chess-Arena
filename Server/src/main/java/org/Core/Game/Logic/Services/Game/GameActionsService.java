package org.Core.Game.Logic.Services.Game;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.Game.Logic.Models.Color;
import org.Core.Game.Logic.Models.GameSession;
import org.Core.Game.Logic.Services.Game.Events.*;
import org.Core.Game.Logic.Services.Ranking.EloSummaries;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GameActionsService {

    private final GameOverHandler gameOverHandler;
    private final GameAuthorizationService authorizationService;
    private final DrawOfferStore drawOfferStore;
    private final ApplicationEventPublisher eventPublisher;


    public void resign(String playerInternalId,String gameId){
        GameSession session=authorizationService.AuthorizePlayer(playerInternalId,gameId);
        String opponentInternalId = Objects.equals(session.getWhitePlayerId(), playerInternalId) ?session.getBlackPlayerId():session.getWhitePlayerId();
        Color opponentColor=Objects.equals(session.getWhitePlayerId(), opponentInternalId) ?Color.WHITE:Color.BLACK;
        // handle()'s playerA/playerB convention is winner-then-loser (matches
        // the checkmate and timeout paths) — the opponent is the winner here.
        Optional<EloSummaries> eloChange=gameOverHandler.handle(gameId,opponentInternalId, playerInternalId,opponentColor, GameOverInfo.EndReason.RESIGNATION);
        GameOverInfo winner=new GameOverInfo(GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.RESIGNATION, eloChange.map(EloSummaries::playerA).orElse(null));
        GameOverInfo loser =new GameOverInfo(GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.RESIGNATION, eloChange.map(EloSummaries::playerB).orElse(null));
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
        Optional<EloSummaries> eloSummaries=gameOverHandler.handle(gameId, playerInternalId, opponentInternalId,Color.NONE, GameOverInfo.EndReason.DRAW_AGREEMENT);
        GameOverInfo player=new GameOverInfo(GameOverInfo.GameResult.DRAW, GameOverInfo.EndReason.DRAW_AGREEMENT, eloSummaries.map(EloSummaries::playerA).orElse(null));
        GameOverInfo opponent=new GameOverInfo(GameOverInfo.GameResult.DRAW, GameOverInfo.EndReason.DRAW_AGREEMENT, eloSummaries.map(EloSummaries::playerB).orElse(null));
        int playerPublicId =playerInternalId.equals(session.getWhitePlayerId())?session.getWhitePlayerPublicId():session.getBlackPlayerPublicId();
        int OpponentPublicId= playerInternalId.equals(session.getWhitePlayerId())?session.getBlackPlayerPublicId():session.getWhitePlayerPublicId();
        Id opponentId=new Id(opponentInternalId,OpponentPublicId);
        Id playerId=new Id(playerInternalId,playerPublicId);
        eventPublisher.publishEvent(new GameOverEvent(new Event(playerId,player),new Event(opponentId,opponent)));
    }



}
