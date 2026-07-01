package org.Core.User.Api.Controllers;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class LobbyPingingController {

    @MessageMapping("/in.lobby")
    public void handleInLobby(Principal principal){
        System.out.println(principal.getName()+" IN LOBBY");
    }


}
