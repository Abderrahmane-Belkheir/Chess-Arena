package org.Core.Social.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsList;
import org.Core.Social.Api.Dto.InvitationsList;
import org.Core.Social.Services.FriendShipQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/social")
public class FriendShipQueryController {

    private final FriendShipQuery friendShipQuery;

    @GetMapping("/friends")
    public ResponseEntity<FriendsList> getFriends() {
        return ResponseEntity.ok(friendShipQuery.getFriends());
    }

    @GetMapping("/invitations")
    public ResponseEntity<InvitationsList> getInvitations(){
        return ResponseEntity.ok(friendShipQuery.getInvitations());
    }

}
