package org.Core.Social.Api.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Services.Game.Events.SpectateRequest;
import org.Core.Social.Game.GameSpectator;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class GameSpectatorController {

    private final GameSpectator gameSpectator;

    @MessageMapping("/spectate.request")
    public void reqSpectate(Principal principal, @Payload @Valid SpectateRequest request){
        gameSpectator.requestSpectate(principal.getName(),request.getUserId());
    }

    @MessageMapping("/spectate.accept")
    public void accSpectate(Principal principal, @Payload @Valid SpectateRequest request){
        gameSpectator.acceptSpectate(principal.getName(),request.getUserId());
    }



}