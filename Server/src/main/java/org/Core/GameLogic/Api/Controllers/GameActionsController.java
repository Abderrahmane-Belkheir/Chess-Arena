package org.Core.GameLogic.Api.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameActionRequest;
import org.Core.GameLogic.Services.Game.GameActionsService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GameActionsController {

    private final GameActionsService actionsService;

    @MessageMapping("/game.resign")
    public void resign(Principal principal, @Payload @Valid GameActionRequest request) {
     actionsService.resign(principal.getName(),request.getGameId());
    }

    @MessageMapping("/game.draw.offer")
    public void offerDraw(Principal principal, @Payload @Valid GameActionRequest request) {
        actionsService.offerDraw(principal.getName(),request.getGameId());
    }

    @MessageMapping("/game.draw.accept")
    public void acceptDraw(Principal principal, @Payload @Valid GameActionRequest request) {
        actionsService.acceptDraw(principal.getName(),request.getGameId());
    }

}
