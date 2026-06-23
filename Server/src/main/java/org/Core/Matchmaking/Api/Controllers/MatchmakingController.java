package org.Core.Matchmaking.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.GameFound;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchmakingController {

    private final SimpMessagingTemplate messagingTemplate;


    @MessageMapping("/in.lobby")
    public void handleInLobby(Principal principal){
        System.out.println(principal.getName()+" IN LOBBY");
    }

    @MessageMapping("/start.search")
    public void handleGameSearchStart(Principal principal){
        System.out.println(principal.getName()+" SEARCHING GAME");
        messagingTemplate.convertAndSendToUser(principal.getName(),"/queue/matchmaking",new GameFound(true,"123",new GameFound.Opponent(123,"ilham",1200,""),"FEN"));
    }

    @MessageMapping("/stop.search")
    public void handleGameSearchStop(Principal principal){
        System.out.println(principal.getName()+" STOPPED SEARCHING GAME");

    }
}
