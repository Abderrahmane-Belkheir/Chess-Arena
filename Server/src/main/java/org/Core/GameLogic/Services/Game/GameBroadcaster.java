package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameFound;
import org.Core.GameLogic.Services.Game.Events.GameOverInfo;

import org.Core.GameLogic.Services.Game.Events.*;
import org.springframework.context.event.EventListener;
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
    public void broadCastGameFound(GameCreatedEvent event){
        handleGameFound(event.whiteId(),event.whiteSession(),event.forWhite());
        handleGameFound(event.blackId(),event.blackSession(),event.forBlack());
    }

    @TransactionalEventListener
    public void broadCastGameOver(GameOverEvent event){
        handleGameEvent(event.playerA().getUserId(),event.playerA());
        handleGameEvent(event.playerB().getUserId(),event.playerB());
    }

    @TransactionalEventListener
    public void broadCastMove(MoveEvent event){
        handleGameEvent(event.userId(),event.response());
    }

    @TransactionalEventListener
    public void broadCastMoveConfirmation(MoveConfirmationEvent event){
        handleGameEvent(event.userId(),event.confirmation());
    }



    @TransactionalEventListener
    public void broadCastDrawOffered(DrawOfferEvent event){
        handleGameEvent(event.getUserId(),event);
    }

    @EventListener
    public void broadCastSpectateRequested(SpectateRequestEvent event){
        handleGameEvent(event.userId(),event.request());
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

    private void handleGameEvent(String userId,GameEvent event){
        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/game.events",
                event
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


