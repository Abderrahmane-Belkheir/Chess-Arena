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
@RequestMapping("/api/v1/game/challenge")
public class ChallengeController {

    private final ChallengeService challengeService;


    @PostMapping("/request")
    public ResponseEntity<Void> challenge(@RequestParam int publicId){
        challengeService.challenge(publicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> accept(@RequestParam int publicId){
        challengeService.accept(publicId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> reject(@RequestParam int publicId){
        challengeService.reject(publicId);
        return ResponseEntity.noContent().build();
    }

}
