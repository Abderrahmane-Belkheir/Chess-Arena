package org.Core.GameLogic.Services.Matchmaking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameFound;
import org.Core.GameLogic.Api.Dto.MoveResponse;
import org.Core.GameLogic.Services.Matchmaking.Events.GameCreatedEvent;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameBroadcaster {

    private final ObjectMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;

    @TransactionalEventListener
    public void broadCastMove(MoveResponse response){
        handleMove();
    }

    @TransactionalEventListener
    public void broadCastGameFound(GameCreatedEvent event){
        handleGameFound(event.whiteId(),event.whiteSession(),event.forWhite());
        handleGameFound(event.blackId(),event.blackSession(),event.forBlack());
    }

    @TransactionalEventListener
    public void broadCastGameNotFound(Object o){

    }

    private void handleGameFound(String userId,String sessionId, GameFound gameFound){
        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);

        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/matchmaking",
                mapper.writeValueAsString(gameFound),
                accessor.getMessageHeaders()
        );
    }

    private void handleMove(){

    }

    }

