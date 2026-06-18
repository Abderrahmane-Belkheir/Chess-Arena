package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Models.FriendShip;
import org.Core.Social.Models.FriendShip_Request;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Persistence.FriendShip_RequestRepo;
import org.Core.User.Services.AuthenticatedUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendShipManager {

    private final FriendShipRepo friendShipRepo;
    private final FriendShip_RequestRepo friendShip_requestRepo;
    private final AuthenticatedUserService authenticatedUserService;

    public void invite(String userId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        if(friendShipRepo.doesFriendShipExists(currentUserId,userId)) throw  new RuntimeException();
        if(friendShip_requestRepo.existBySenderIdAndReceiverId(currentUserId,userId)) return;
        if(friendShip_requestRepo.existBySenderIdAndReceiverId(userId,currentUserId)) throw new RuntimeException();
        FriendShip_Request request=new FriendShip_Request(currentUserId,userId);
        friendShip_requestRepo.save(request);
        // TODO
        // DELIVERING NOTIFICATION TO RECIPIENT
    }


    public void accept(String userId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        if(friendShipRepo.doesFriendShipExists(currentUserId,userId)) return;
        Optional<FriendShip_Request> request=friendShip_requestRepo.findByBySenderIdAndReceiverId(userId,currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShip_requestRepo.delete(request.get());
        FriendShip friendShip=new FriendShip(currentUserId,userId);
        friendShipRepo.save(friendShip);
        // TODO
        // DELIVERING NOTIFICATION TO RECIPIENT
    }

    public void reject(String userId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        if(friendShipRepo.doesFriendShipExists(currentUserId,userId)) return;
        Optional<FriendShip_Request> request=friendShip_requestRepo.findByBySenderIdAndReceiverId(userId,currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShip_requestRepo.delete(request.get());;
    }

    public void deleteFriend(String userId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Optional<FriendShip> request=friendShipRepo.findFriendShip(userId,currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShipRepo.delete(request.get());
    }
}
