package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameFound;
import org.Core.GameLogic.Api.Dto.GameOverInfo;
import org.Core.GameLogic.Api.Dto.MoveConfirmation;
import org.Core.GameLogic.Api.Dto.MoveResponse;
import org.Core.GameLogic.Services.Game.Events.GameCreatedEvent;
import org.Core.GameLogic.Services.Game.Events.GameOverEvent;
import org.Core.GameLogic.Services.Game.Events.MoveConfirmationEvent;
import org.Core.GameLogic.Services.Game.Events.MoveEvent;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameBroadcaster {


    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    public void broadCastGameOver(GameOverEvent event){
        handleGameOver(event.playerA().getUserId(),event.playerA());
        handleGameOver(event.playerB().getUserId(),event.playerB());
    }

    @TransactionalEventListener
    public void broadCastMove(MoveEvent event){
        handleMove(event.userId(),event.response());
    }

    @TransactionalEventListener
    public void broadCastMoveConfirmation(MoveConfirmationEvent event){
        handleConfirmMove(event.userId(),event.confirmation());
    }

    @TransactionalEventListener
    public void broadCastGameFound(GameCreatedEvent event){
        handleGameFound(event.whiteId(),event.whiteSession(),event.forWhite());
        handleGameFound(event.blackId(),event.blackSession(),event.forBlack());
    }

    @TransactionalEventListener
    public void broadCastGameNotFound(MoveConfirmationEvent event){

    }

    private void handleGameFound(String userId,String sessionId, GameFound gameFound){
        SimpMessageHeaderAccessor accessor =setAccessor(sessionId);

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/matchmaking",
                gameFound,
                accessor.getMessageHeaders()
        );
    }

    private void handleMove(String userId, MoveResponse response){
       // SimpMessageHeaderAccessor accessor=setAccessor(sessionId);

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/game.move",
                response
        );
    }

    private void handleConfirmMove(String userId,MoveConfirmation confirmation){
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/game.move.confirm",
                confirmation
        );
    }

    private void handleGameOver(String userId,GameOverInfo gameOverInfo){
        messagingTemplate.convertAndSendToUser(
              userId,
                "/queue/game.over",
                gameOverInfo
        );
    }


    private SimpMessageHeaderAccessor setAccessor(String sessionId){
        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return accessor;
    }

}


