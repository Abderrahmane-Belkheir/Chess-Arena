package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Services.Game.Events.Event;
import org.Core.GameLogic.Services.Game.Events.GameOverInfo;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Persistence.GameRepo;
import org.Core.GameLogic.Services.Game.Events.GameOverEvent;
import org.Core.GameLogic.Services.Game.Events.Id;
import org.Core.GameLogic.Services.MoveValidation.GameSessionRegistry;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameOverHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final GameRepo gameRepo;
    private final GameSessionStore gameSessionStore;
    private final GameSessionRegistry gameSessionRegistry;
    private final UserRepo userRepo;


    public void handle(String gameId,String playerA,String playerB,  Color winnerColor, GameOverInfo.EndReason endReason) {

            boolean isDraw=endReason== GameOverInfo.EndReason.DRAW||endReason== GameOverInfo.EndReason.DRAW_AGREEMENT;
            Game.Result result=isDraw? Game.Result.DRAW: winnerColor ==Color.WHITE? Game.Result.WHITE_WIN: Game.Result.BLACK_WIN;
            endGame(new EndGame(gameId,playerA,playerB,result,endReason));
}

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleTimeOut(String gameId, Id winnerId, Id loserId, Color winnerColor){
        Game.Result result= winnerColor==Color.WHITE?Game.Result.WHITE_WIN:Game.Result.BLACK_WIN;
        endGame(new EndGame(gameId,winnerId.internalId(),loserId.internalId(),result, GameOverInfo.EndReason.TIMEOUT));
        GameOverInfo winner=new GameOverInfo(GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.TIMEOUT);
        GameOverInfo looser=new GameOverInfo(GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.TIMEOUT);
        eventPublisher.publishEvent(new GameOverEvent(new Event(loserId,looser),new Event(winnerId,winner)));
    }

    private void endGame(EndGame endGame){
        gameRepo.endGame(endGame.gameId, endGame.result,Game.GameStatus.ENDED,endGame.endReason, Instant.now());
        userRepo.updateUsersStatus(User.Status.IN_LOBBY,endGame.playerA, endGame.playerB);
        gameSessionRegistry.removeSession(endGame.gameId);
        gameSessionStore.remove(endGame.gameId);
    }

    record EndGame(String gameId,String playerA,String playerB ,Game.Result result, GameOverInfo.EndReason endReason){}

}