package org.Core.Social.Api.Controllers;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Services.FriendShipManager;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class FriendShipManagerController {

    private final FriendShipManager friendShipManager;

}
