package org.Core.GameLogic.Api.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.ResignRequest;
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
    public void resign(Principal principal, @Payload @Valid ResignRequest request) {
        log.info("RESIGNING GAME "+request.getGameId());
     actionsService.resign(principal.getName(),request.getGameId());
    }

}
