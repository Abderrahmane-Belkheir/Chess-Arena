package org.Core.GameLogic.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Services.Matchmaking.MatchmakingEntry;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingEntry matchmakingService;

    @MessageMapping("/start.search")
    public void handleGameSearchStart(Principal principal) throws InterruptedException {
       matchmakingService.searchGame(principal.getName());
    }

    @MessageMapping("/stop.search")
    public void handleGameSearchStop(Principal principal){
        System.out.println(principal.getName()+" STOPPED SEARCHING GAME");
    }

}
