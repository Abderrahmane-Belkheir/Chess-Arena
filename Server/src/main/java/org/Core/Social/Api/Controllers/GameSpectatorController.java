package org.Core.Social.Api.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.Core.Social.Game.GameSpectator;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class GameSpectatorController {

    private final GameSpectator gameSpectator;

//    @MessageMapping("/spectate.request")
//    public void reqSpectate(Principal principal, @Payload @Valid){
//        gameSpectator.requestSpectate(null,null);
//    }

//    @MessageMapping("/spectate.accept")
//    public void accSpectate(Principal principal,@Payload @Valid){
//        gameSpectator.acceptSpectate(null,null);
//    }



}