package org.Core.Game.Logic.Api.Controllers;

import lombok.RequiredArgsConstructor;

import org.Core.Game.Logic.Api.Dto.UserSession;
import org.Core.Game.Logic.Services.Matchmaking.MatchmakingEntry;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class MatchmakingController {

    private final MatchmakingEntry matchmakingService;

    @MessageMapping("/start.search")
    public void handleGameSearchStart(Principal principal, SimpMessageHeaderAccessor headerAccessor){
       matchmakingService.searchGame(new UserSession(principal.getName(),headerAccessor.getSessionId()));
    }

    @MessageMapping("/stop.search")
    public void handleGameSearchStop(Principal principal){
        matchmakingService.stopSearch(principal.getName());
    }

}
