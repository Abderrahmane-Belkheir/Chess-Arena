package org.Core.Social.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsPage;
import org.Core.Social.Services.FriendShipQuery;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/social")
public class FriendShipQueryController {

    private final FriendShipQuery friendShipQuery;
    @GetMapping("/friends")
    public ResponseEntity<FriendsPage> get(@RequestParam(required = false) String cursor) {

        return ResponseEntity.ok(new FriendsPage(new ArrayList<>(), "page2", true));
    }

}
