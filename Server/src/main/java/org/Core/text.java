package org.Core;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class text {


    @MessageMapping("/online")
    public void heartbeat(Principal principal){
      System.out.println(principal.getName()+" IN LOBBY");
    }

    @MessageMapping("/play")
    public void heartbeat1(Principal principal){
        System.out.println(principal.getName()+" REQUESTING TO PLAY");
    }

}
