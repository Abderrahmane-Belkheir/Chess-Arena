package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsPage;
import org.Core.Social.Api.Dto.InvitationsPage;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Persistence.FriendShip_RequestRepo;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FriendShipQuery {

    private final FriendShipRepo friendShipRepo;
    private final FriendShip_RequestRepo friendShip_requestRepo;

    public FriendsPage getOnlineFriends(String cursor){
        return null;
    }

    public FriendsPage getOfflineFriends(String cursor){

        return null;
    }

    public InvitationsPage getInvitations(String cursor){
        return null;
    }
}
