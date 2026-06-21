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
    public ResponseEntity<Void> invite(@RequestParam int publicId){
        friendShipManager.invite(publicId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/unSend")
    public ResponseEntity<Void> unSend(@RequestParam int publicId){
        friendShipManager.unSend(publicId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/accept")
    public ResponseEntity<Void> accept(@RequestParam int publicId){
        friendShipManager.accept(publicId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/reject")
    public ResponseEntity<Void> reject(@RequestParam int publicId){
        friendShipManager.reject(publicId);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteFriend(@RequestParam int publicId){
        friendShipManager.deleteFriend(publicId);
        return ResponseEntity.noContent().build();
    }

}
