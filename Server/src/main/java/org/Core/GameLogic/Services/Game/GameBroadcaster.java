package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameFound;

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
        handleGameEvent(event.playerA().id().internalId(),event.playerA().event());
        handleGameEvent(event.playerB().id().internalId(),event.playerB().event());
        broadCastToSpectators(event.playerA().id().publicId(),event.playerA().event());
        broadCastToSpectators(event.playerB().id().publicId(),event.playerB().event());
    }

    @TransactionalEventListener
    public void broadCastGameEvent(Event event){
        handleGameEvent(event.id().internalId(),event.event());
        if(event.event() instanceof DrawOfferEvent || event.event() instanceof SpectatedResponse){return;}
        broadCastToSpectators(event.id().publicId(),event.event());
    }


    @EventListener
    public void broadCastSpectateAccepted(SpectatorResponse event){
        handleSpectatorResponse(event.targetId(),event);
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

    private void handleSpectatorResponse(String userId,SpectatorResponse response){
        messagingTemplate.convertAndSendToUser(
                userId,
                "queue/spectate.responses",
                response
        );
    }

    private void broadCastToSpectators(int userId,GameEvent event){
        messagingTemplate.convertAndSend("/topic/spectate/"+userId,event);
    }

    private SimpMessageHeaderAccessor setAccessor(String sessionId){
        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return accessor;
    }

}


