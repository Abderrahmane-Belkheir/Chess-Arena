package org.Core.Game.Logic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.Game.Logic.Exceptions.GameNotFoundException;
import org.Core.Game.Logic.Models.Game;
import org.Core.Game.Logic.Models.GameSession;
import org.Core.Game.Logic.Persistence.GameRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameAuthorizationService {


    private final GameSessionStore gameSessionStore;
    private final GameRepo gameRepo;

    public GameSession AuthorizePlayer(String userId, String gameId){
    GameSession session=gameSessionStore.find(gameId).orElse(restoreGameSession(gameId));
    boolean belongsToGame = session.getWhitePlayerId().equals(userId)
            || session.getBlackPlayerId().equals(userId);
        if(!belongsToGame) {
        log.warn("Player {} attempted to move in game {} they don't belong to",
                userId, gameId);
        throw new GameNotFoundException("Game not found");
    }
        return session;
    }

        private GameSession restoreGameSession(String gameId){
            Game game=gameRepo.findById(gameId).orElseThrow();
            //TODO restoring the game session
            return null;
        }


}
