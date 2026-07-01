package org.Core.GameLogic.Services.Authorazation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.MoveRequest;
import org.Core.GameLogic.Exceptions.GameNotFoundException;
import org.Core.GameLogic.Exceptions.WrongTurnException;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Models.Player;
import org.Core.GameLogic.Services.MoveValidation.GameMoveValidation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameAuthorizationService {

    private final GameMoveValidation gameMoveValidation;
    private final GameSessionStore gameSessionStore;

    public void Authorize(String userId, MoveRequest request){
        GameSession session=gameSessionStore.find(request.getGameId()).orElse(null);
        if(session==null){return;}
        boolean belongsToGame = session.getWhitePlayerId().equals(userId)
                || session.getBlackPlayerId().equals(userId);
        if(!belongsToGame) {
            log.warn("Player {} attempted to move in game {} they don't belong to",
                    userId, request.getGameId());
            throw new GameNotFoundException("Game not found");
        }
        Player.Color playerColor= session.getWhitePlayerId().equals(userId)? Player.Color.WHITE: Player.Color.BLACK;
        if(playerColor!=session.getTurn()) throw new WrongTurnException("Not your turn");
        // TODO here i should first look for the instance where the board is created
        //  if its the current continue if not route it to the right instance
        gameMoveValidation.validateAndPlay(request);
        // TODO deliver move to opponent
    }

}
