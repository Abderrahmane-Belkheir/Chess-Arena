package org.Core.GameLogic.Services.Authorization;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameOverInfo;
import org.Core.GameLogic.Api.Dto.MoveOutCome;
import org.Core.GameLogic.Api.Dto.MoveRequest;
import org.Core.GameLogic.Exceptions.GameNotFoundException;
import org.Core.GameLogic.Exceptions.WrongTurnException;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Persistence.GameRepo;
import org.Core.GameLogic.Services.Matchmaking.Events.GameOverEvent;
import org.Core.GameLogic.Services.Matchmaking.Events.MoveEvent;
import org.Core.GameLogic.Services.MoveValidation.GameMoveValidation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameAuthorizationService {

    private final GameMoveValidation gameMoveValidation;
    private final GameSessionStore gameSessionStore;
    private final GameRepo gameRepo;
    private final ApplicationEventPublisher eventPublisher;

    private final static long TEN_MINUTES_MS = TimeUnit.MINUTES.toMillis(10);
    private final static long THEE_MINUTES_MS=TimeUnit.MINUTES.toMillis(3);

    @Transactional
    public void Authorize(String userId, MoveRequest request){
        GameSession session=gameSessionStore.find(request.getGameId()).orElseGet(()->{
            return null;
        });
        if(session==null){return;}
        boolean belongsToGame = session.getWhitePlayerId().equals(userId)
                || session.getBlackPlayerId().equals(userId);
        if(!belongsToGame) {
            log.warn("Player {} attempted to move in game {} they don't belong to",
                    userId, request.getGameId());
            throw new GameNotFoundException("Game not found");
        }
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
        if(TEN_MINUTES_MS<playedTime){
            handleTimeOut(userId,opponentId);
        }
        MoveOutCome outCome=gameMoveValidation.processMove(request);
        gameSessionStore.updateTurnAndPlayedTimeAndLastMoveAt(request.getGameId(),session.getTurn()==Color.BLACK?Color.WHITE:Color.BLACK,now,playedTime);
        if(outCome.gameOver()){
            GameOverInfo moverGameOverInfo=outCome.moverGameOverInfo();
            eventPublisher.publishEvent(new GameOverEvent(userId,moverGameOverInfo));
        }
        eventPublisher.publishEvent(new MoveEvent(opponentId,outCome.opponentPayload()));
    }

    private void handleTimeOut(String userId,String opponentId){
        GameOverInfo winner=new GameOverInfo(GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.TIMEOUT);
        GameOverInfo looser=new GameOverInfo(GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.TIMEOUT);

        eventPublisher.publishEvent(new GameOverEvent(opponentId,winner));
        eventPublisher.publishEvent(new GameOverEvent(userId,looser));
    }

}
