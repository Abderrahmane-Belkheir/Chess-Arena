package org.Core.GameLogic.Services.Game;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.Core.GameLogic.Services.Game.Events.MoveEvent;
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

    private final GameOverHandler gameOverHandler;
    private final GameMoveValidation gameMoveValidation;
    private final GameSessionStore gameSessionStore;
    private final GameRepo gameRepo;
    private final GameMoveRepo gameMoveRepo;
    private final ApplicationEventPublisher eventPublisher;

    private final static long TEN_MINUTES_MS = TimeUnit.MINUTES.toMillis(10);
    private final static long THEE_MINUTES_MS=TimeUnit.MINUTES.toMillis(3);

    @Transactional
    public void AuthorizeAndPersist(String userId, MoveRequest request){
        String gameId=request.getGameId();
        GameSession session=gameSessionStore.find(gameId).orElse(restoreGameSession(gameId));

        if(session==null){return;}
        boolean belongsToGame = session.getWhitePlayerId().equals(userId)
                || session.getBlackPlayerId().equals(userId);
        if(!belongsToGame) {
            log.warn("Player {} attempted to move in game {} they don't belong to",
                    userId, gameId);
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
            Color opponentColor=playerColor==Color.WHITE?Color.BLACK:Color.WHITE;
            gameOverHandler.handleTimeOut(gameId,userId,opponentId,opponentColor);
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
        gameOverHandler.checkAndHandle(gameId,userId,playerColor,outCome);
        eventPublisher.publishEvent(new MoveEvent(opponentId,outCome.opponentPayload()));
    }


    private GameSession restoreGameSession(String gameId){
        Game game=gameRepo.findById(gameId).orElseThrow();
        //TODO restoring the game session
        return null;
    }

}
