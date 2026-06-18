package org.Core.Social.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Services.FriendShipManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/social")
public class FriendShipManagerController {

    private final FriendShipManager friendShipManager;

    @PostMapping("/invite")
    public ResponseEntity<Void> invite(@RequestParam String userId ){
        friendShipManager.invite(userId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/accept")
    public ResponseEntity<Void> accept(@RequestParam String userId){
        friendShipManager.accept(userId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/reject")
    public ResponseEntity<Void> reject(@RequestParam String userId){
        friendShipManager.reject(userId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFriend(@RequestParam String userId){
        friendShipManager.deleteFriend(userId);
        return ResponseEntity.noContent().build();
    }

}
