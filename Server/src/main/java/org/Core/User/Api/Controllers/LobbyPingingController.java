package org.Core.User.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.User.Services.PresenceStore;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class LobbyPingingController {

    private final PresenceStore presenceStore;

    @MessageMapping("/online")
    public void handleInLobby(Principal principal){
        presenceStore.markOnline(principal.getName());
    }


}
