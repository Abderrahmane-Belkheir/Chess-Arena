package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Persistence.FriendShip_RequestRepo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendShipManager {

    private final FriendShipRepo friendShipRepo;
    private final FriendShip_RequestRepo friendShip_requestRepo;

}
