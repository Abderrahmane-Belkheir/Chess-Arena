package org.Core.GameLogic.Services.Game;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.MoveConfirmation;
import org.Core.GameLogic.Api.Dto.MoveOutCome;
import org.Core.GameLogic.Api.Dto.MoveRequest;
import org.Core.GameLogic.Exceptions.GameNotFoundException;
import org.Core.GameLogic.Exceptions.WrongTurnException;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Models.GameMove;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Persistence.GameMoveRepo;
import org.Core.GameLogic.Persistence.GameRepo;
import org.Core.GameLogic.Services.Game.Events.MoveConfirmationEvent;
import org.Core.GameLogic.Services.Game.Events.MoveEvent;
import org.Core.GameLogic.Services.MoveValidation.GameMoveValidation;
import org.Core.Scheduling.TimeOutSchedulingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

import static org.Core.GameLogic.Utilities.TEN_MINUTES_MS;
import static org.Core.GameLogic.Utilities.THREE_MINUTES_MS;

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
    public void AuthorizeAndPersist(String userId, MoveRequest request){
        String gameId=request.getGameId();
        GameSession session=authorizationService.AuthorizePlayer(userId,gameId);
        Color playerColor= session.getWhitePlayerId().equals(userId)? Color.WHITE: Color.BLACK;
        if(playerColor!=session.getTurn()){
            //TODO
            throw new WrongTurnException("Not your turn");
        }
        long playedTime=playerColor==Color.WHITE?session.getWhitePlayedTime():session.getBlackPlayedTime();
        Instant now= Instant.now();
        long durationToPlay =Duration.between(session.getLastMoveAt(),now).toMillis();
        playedTime+= durationToPlay;
        String opponentId=playerColor==Color.WHITE?session.getBlackPlayerId():session.getWhitePlayerId();
        Color opponentColor=playerColor==Color.WHITE?Color.BLACK:Color.WHITE;
        long gameDuration=session.getType()== Game.GameType.RAPID?TEN_MINUTES_MS:THREE_MINUTES_MS;
        if(gameDuration<playedTime){
            timeOutSchedulingService.cancel(gameId);
            gameOverHandler.handleTimeOut(gameId,opponentId,userId,opponentColor);
            return;
        }
        MoveOutCome outCome=gameMoveValidation.processMove(request);
        gameSessionStore.updateTurnAndPlayedTimeAndLastMoveAt(gameId,session.getTurn()==Color.BLACK?Color.WHITE:Color.BLACK,now,playedTime);
        String from=outCome.opponentPayload().from();
        String to=outCome.opponentPayload().to();
        String newFen=outCome.newFen();
        gameMoveRepo.save(GameMove.builder()
                .game(gameRepo.getReferenceById(gameId)).fromSquare(from).toSquare(to).color(playerColor).fenAfter(newFen).timeToPlay(durationToPlay).
                build());
        gameRepo.updateFen(request.getGameId(),newFen);
        long opponentPlayedTime=playerColor==Color.WHITE?session.getBlackPlayedTime(): session.getWhitePlayedTime();
       if(outCome.gameOver()){
           timeOutSchedulingService.cancel(gameId);
           gameOverHandler.handle(gameId,playerColor,outCome.moverGameOverEvent().getEndReason());
       }else {
           timeOutSchedulingService.schedule(gameId,gameDuration-opponentPlayedTime,()->gameOverHandler.handleTimeOut(gameId,userId,opponentId,playerColor));
       }
        eventPublisher.publishEvent(new MoveEvent(opponentId,outCome.opponentPayload()));
        eventPublisher.publishEvent(new MoveConfirmationEvent(userId,
                new MoveConfirmation(newFen, gameDuration-playedTime, gameDuration-opponentPlayedTime,outCome.moverGameOverEvent())));
        drawOfferStore.clear(gameId);
    }




}
