package org.Core.Game.Social;

import lombok.RequiredArgsConstructor;
import org.Core.User.Services.AuthenticatedUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/game/spectate")
public class GameSpectatorController {

    private final GameSpectatorService gameSpectator;
    private final AuthenticatedUserService authenticatedUserService;

    @PostMapping("/request")
    public ResponseEntity<Void> reqSpectate(@RequestParam int userId){
        gameSpectator.requestSpectate(authenticatedUserService.getCurrentUser(), userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> accSpectate(@RequestParam int spectatorId){
        gameSpectator.acceptSpectate(authenticatedUserService.getCurrentUser(), spectatorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> rejSpectate(@RequestParam int spectatorId){
        gameSpectator.rejectSpectate(authenticatedUserService.getCurrentUser(), spectatorId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/quit")
    public ResponseEntity<Void> quitSpectating(@RequestParam int targetId){
        gameSpectator.quitSpectating(authenticatedUserService.getCurrentUser(), targetId);
        return ResponseEntity.noContent().build();
    }

}
