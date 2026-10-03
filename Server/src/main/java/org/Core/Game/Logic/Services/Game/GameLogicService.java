package org.Core.Game.Logic.Services.Game;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.Game.Logic.Api.Dto.MoveOutCome;
import org.Core.Game.Logic.Api.Dto.MoveRequest;
import org.Core.Game.Logic.Exceptions.WrongTurnException;
import org.Core.Game.Logic.Models.Color;
import org.Core.Game.Logic.Models.Game;
import org.Core.Game.Logic.Models.GameMove;
import org.Core.Game.Logic.Models.GameSession;
import org.Core.Game.Logic.Persistence.GameMoveRepo;
import org.Core.Game.Logic.Persistence.GameRepo;
import org.Core.Game.Logic.Services.Game.Events.Event;
import org.Core.Game.Logic.Services.Game.Events.Id;
import org.Core.Game.Logic.Services.Game.Events.MoveConfirmation;
import org.Core.Game.Logic.Services.MoveValidation.GameMoveValidation;
import org.Core.Game.Logic.Services.Ranking.EloSummaries;
import org.Core.Scheduling.TimeOutSchedulingService;
import org.Core.Social.Services.FriendStatusBroadcaster;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.Core.Game.Logic.Utilities.TEN_MINUTES_MS;
import static org.Core.Game.Logic.Utilities.THREE_MINUTES_MS;


@Service
@RequiredArgsConstructor
@Slf4j
public class GameLogicService {

    private final GameOverHandler gameOverHandler;
    private final GameMoveValidation gameMoveValidation;
    private final GameSessionStore gameSessionStore;
    private final GameRepo gameRepo;
    private final GameMoveRepo gameMoveRepo;
    private final TimeOutSchedulingService timeOutSchedulingService;
    private final ApplicationEventPublisher eventPublisher;
    private final GameAuthorizationService authorizationService;
    private final DrawOfferStore drawOfferStore;




    @Transactional
    public void AuthorizeAndPersist(String playerInternalId, MoveRequest request){
        String gameId=request.getGameId();
        GameSession session=authorizationService.AuthorizePlayer(playerInternalId,gameId);
        Color playerColor= session.getWhitePlayerId().equals(playerInternalId)? Color.WHITE: Color.BLACK;
        if(playerColor!=session.getTurn()){
            throw new WrongTurnException("Not your turn");
        }
        long playedTime=playerColor==Color.WHITE?session.getWhitePlayedTime():session.getBlackPlayedTime();
        Instant now= Instant.now();
        long durationToPlay =Duration.between(session.getLastMoveAt(),now).toMillis();
        playedTime+= durationToPlay;
        String opponentInternalId=playerColor==Color.WHITE?session.getBlackPlayerId():session.getWhitePlayerId();
        Color opponentColor=playerColor==Color.WHITE?Color.BLACK:Color.WHITE;
        long gameDuration=session.getType()== Game.GameType.RAPID?TEN_MINUTES_MS:THREE_MINUTES_MS;
        int playerPublicId =playerColor==Color.WHITE?session.getWhitePlayerPublicId():session.getBlackPlayerPublicId();
        int OpponentPublicId=opponentColor==Color.WHITE?session.getWhitePlayerPublicId():session.getBlackPlayerPublicId();
        Id opponentId=new Id(opponentInternalId,OpponentPublicId);
        Id playerId=new Id(playerInternalId,playerPublicId);
        if(gameDuration<playedTime){
            gameOverHandler.handleTimeOut(gameId,opponentId,playerId,opponentColor);
            return;
        }
        MoveOutCome outCome=gameMoveValidation.processMove(request);
        gameSessionStore.updateTurnAndPlayedTimeAndLastMoveAt(gameId,session.getTurn()==Color.BLACK?Color.WHITE:Color.BLACK,now,playedTime);
        String from=outCome.opponentPayload().getFrom();
        String to=outCome.opponentPayload().getTo();
        String newFen=outCome.newFen();
        gameMoveRepo.save(GameMove.builder()
                .game(gameRepo.getReferenceById(gameId)).fromSquare(from).toSquare(to).color(playerColor).fenAfter(newFen).timeToPlay(durationToPlay).
                build());
        gameRepo.updateFen(request.getGameId(),newFen);
        long opponentPlayedTime=playerColor==Color.WHITE?session.getBlackPlayedTime(): session.getWhitePlayedTime();

       if(outCome.gameOver()){
           // handle()'s playerA/playerB convention is winner-then-loser — the
           // mover just delivered checkmate/stalemate etc., so they're playerA.
           gameOverHandler.handle(gameId,playerInternalId,opponentInternalId,playerColor,outCome.moverGameOverEvent().getEndReason())
                   .ifPresent(es -> {
                       outCome.moverGameOverEvent().setEloSummary(es.playerA());
                       outCome.opponentPayload().getGameOverInfo().setEloSummary(es.playerB());
                   });
       }else {
           timeOutSchedulingService.schedule(gameId,gameDuration-opponentPlayedTime,()->gameOverHandler.handleTimeOut(gameId,playerId,opponentId,playerColor));
       }
        eventPublisher.publishEvent(new Event(opponentId,outCome.opponentPayload()));
        eventPublisher.publishEvent(new Event(playerId,
                new MoveConfirmation(from,to,newFen, gameDuration-playedTime, gameDuration-opponentPlayedTime,outCome.moverGameOverEvent())));
        drawOfferStore.clear(gameId);
    }




}
