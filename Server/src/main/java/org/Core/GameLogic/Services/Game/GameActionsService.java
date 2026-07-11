package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Api.Dto.GameOverInfo;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Services.Game.Events.GameOverEvent;
import org.Core.Scheduling.TimeOutSchedulingService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GameActionsService {

    private final GameOverHandler gameOverHandler;
    private final GameAuthorizationService authorizationService;
    private final ApplicationEventPublisher eventPublisher;
    private final TimeOutSchedulingService timeOutSchedulingService;

    @Transactional
    public void resign(String userId,String gameId){
        GameSession session=authorizationService.AuthorizePlayer(userId,gameId);
        // here its clear that the opponent is the winner so we pass in the handle method which is expecting the winner
        String opponentId= Objects.equals(session.getWhitePlayerId(), userId) ?session.getBlackPlayerId():session.getWhitePlayerId();
        Color opponentColor=Objects.equals(session.getWhitePlayerId(),opponentId) ?Color.WHITE:Color.BLACK;
       gameOverHandler.handle(gameId,opponentColor, GameOverInfo.EndReason.RESIGNATION);
        GameOverInfo winner=new GameOverInfo(opponentId,GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.RESIGNATION);
        GameOverInfo loser =new GameOverInfo(userId,GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.RESIGNATION);
        timeOutSchedulingService.cancel(gameId);
        eventPublisher.publishEvent(new GameOverEvent(loser,winner));
    }

}
