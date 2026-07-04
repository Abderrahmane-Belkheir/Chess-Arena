package org.Core.GameLogic.Services.Authorization;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.MoveRequest;
import org.Core.GameLogic.Api.Dto.MoveResponse;
import org.Core.GameLogic.Exceptions.GameNotFoundException;
import org.Core.GameLogic.Exceptions.WrongTurnException;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Persistence.GameMoveRepo;
import org.Core.GameLogic.Services.Matchmaking.Events.MoveEvent;
import org.Core.GameLogic.Services.MoveValidation.GameMoveValidation;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameAuthorizationService {

    private final GameMoveValidation gameMoveValidation;
    private final GameSessionStore gameSessionStore;
    private final GameMoveRepo gameMoveRepo;
    private final ApplicationEventPublisher eventPublisher;

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
        if(playerColor!=session.getTurn()) throw new WrongTurnException("Not your turn");
        // TODO here i should first look for the instance where the board is created
        //  if its the current continue if not route it to the right instance
        MoveResponse response=gameMoveValidation.validateAndPlay(request);

        gameSessionStore.updateTurn(request.getGameId(),session.getTurn()==Color.BLACK?Color.WHITE:Color.BLACK);
        String opponentId=playerColor==Color.WHITE?session.getBlackPlayerId():session.getWhitePlayerId();
        // TODO deliver move to opponent
        eventPublisher.publishEvent(new MoveEvent(opponentId,response));
    }

}
