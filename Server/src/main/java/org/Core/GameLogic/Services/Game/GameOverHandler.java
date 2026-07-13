package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Services.Game.Events.GameOverInfo;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Persistence.GameRepo;
import org.Core.GameLogic.Services.Game.Events.GameOverEvent;
import org.Core.GameLogic.Services.MoveValidation.GameSessionRegistry;
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


    public void handle(String gameId,  Color winnerColor, GameOverInfo.EndReason endReason) {

            boolean isDraw=endReason== GameOverInfo.EndReason.DRAW||endReason== GameOverInfo.EndReason.DRAW_AGREEMENT;
            Game.Result result=isDraw? Game.Result.DRAW: winnerColor ==Color.WHITE? Game.Result.WHITE_WIN: Game.Result.BLACK_WIN;
            endGame(new EndGame(gameId,result,endReason));
}

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleTimeOut(String gameId,String winnerId,String loserId,Color winnerColor){
        log.info("HANDLING TIMEOUT WINNER:"+winnerId+" LOSER:"+loserId);
        Game.Result result= winnerColor==Color.WHITE?Game.Result.WHITE_WIN:Game.Result.BLACK_WIN;
        endGame(new EndGame(gameId,result, GameOverInfo.EndReason.TIMEOUT));
        GameOverInfo winner=new GameOverInfo(winnerId,GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.TIMEOUT);
        GameOverInfo looser=new GameOverInfo(loserId,GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.TIMEOUT);
        eventPublisher.publishEvent(new GameOverEvent(looser,winner));
    }

    private void endGame(EndGame endGame){
        gameRepo.endGame(endGame.gameId, endGame.result,Game.GameStatus.ENDED,endGame.endReason, Instant.now());
        gameSessionRegistry.removeSession(endGame.gameId);
        gameSessionStore.remove(endGame.gameId);
    }

    record EndGame(String gameId, Game.Result result, GameOverInfo.EndReason endReason){}

}