package org.Core.Matchmaking.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Matchmaking.Api.Dto.GameFound;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchmakingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper mapper;

    @MessageMapping("/in.lobby")
    public void handleInLobby(Principal principal){
        System.out.println(principal.getName()+" IN LOBBY");
    }

    @MessageMapping("/start.search")
    public void handleGameSearchStart(Principal principal){
        GameFound gameFound = new GameFound(true, "123",
                new GameFound.Opponent(123, "ilham", 1200, ""), "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", GameFound.PlayerColor.WHITE);
        String json = mapper.writeValueAsString(gameFound);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/queue/matchmaking",
                json);
    }

    @MessageMapping("/stop.search")
    public void handleGameSearchStop(Principal principal){
        System.out.println(principal.getName()+" STOPPED SEARCHING GAME");
    }

}
