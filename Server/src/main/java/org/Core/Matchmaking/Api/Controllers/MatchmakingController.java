package org.Core.Matchmaking.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Matchmaking.Api.Dto.GameFound;
import org.Core.Matchmaking.Services.MatchmakingService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tools.jackson.databind.ObjectMapper;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingService matchmakingService;


    @MessageMapping("/in.lobby")
    public void handleInLobby(Principal principal){
        System.out.println(principal.getName()+" IN LOBBY");
    }

    @MessageMapping("/start.search")
    public void handleGameSearchStart(Principal principal) throws InterruptedException {
       matchmakingService.searchGame(principal.getName());
    }

    @MessageMapping("/stop.search")
    public void handleGameSearchStop(Principal principal){
        System.out.println(principal.getName()+" STOPPED SEARCHING GAME");
    }

}
