package org.Core.GameLogic.Api.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Api.Dto.MoveRequest;
import org.Core.GameLogic.Services.Game.GameLogicService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class GameMoveController {

    private final GameLogicService gameAuthorizationService;

    @MessageMapping("/game.move")
    public void handleMove(Principal principal, @Payload @Valid MoveRequest request) throws InterruptedException {
        gameAuthorizationService.AuthorizeAndPersist(principal.getName(),request);
    }

}
