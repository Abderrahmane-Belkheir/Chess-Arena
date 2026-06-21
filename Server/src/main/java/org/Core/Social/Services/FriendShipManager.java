package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Exceptions.InvitationRequestException;
import org.Core.Social.Models.FriendShip;
import org.Core.Social.Models.FriendShip_Request;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Persistence.FriendShip_RequestRepo;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
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
    private final UserRepo userRepo;

    public void invite(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        if(friendShipRepo.doesFriendShipExists(currentUserId,userId.getUserId())) return;
        if(friendShip_requestRepo.existsBySenderIdAndRecipientId(currentUserId,userId.getUserId())) return;
        if(friendShip_requestRepo.existsBySenderIdAndRecipientId(userId.getUserId(),currentUserId)) throw new InvitationRequestException("");

        User currentUser=userRepo.getReferenceById(currentUserId);
        User recipientUser=userRepo.getReferenceById(userId.getUserId());
        FriendShip_Request request=new FriendShip_Request(currentUser,recipientUser);
        friendShip_requestRepo.save(request);
        // TODO
        // DELIVERING NOTIFICATION TO RECIPIENT
    }

    public void unSend(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        friendShip_requestRepo.
                findBySenderIdAndRecipientId(currentUserId,userId.getUserId()).
                ifPresentOrElse(friendShip_requestRepo::delete,()->{throw new RuntimeException();});
    }

    public void accept(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        if(friendShipRepo.doesFriendShipExists(currentUserId,userId.getUserId())) return;
        Optional<FriendShip_Request> request=friendShip_requestRepo.findBySenderIdAndRecipientId(userId.getUserId(),currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShip_requestRepo.delete(request.get());
        User currentUser=userRepo.getReferenceById(currentUserId);
        User recipientUser=userRepo.getReferenceById(userId.getUserId());
        FriendShip friendShip=new FriendShip(currentUser,recipientUser);
        friendShipRepo.save(friendShip);
        // TODO
        // DELIVERING NOTIFICATION TO RECIPIENT
    }

    public void reject(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        if(friendShipRepo.doesFriendShipExists(currentUserId,userId.getUserId())) return;
        Optional<FriendShip_Request> request=friendShip_requestRepo.findBySenderIdAndRecipientId(userId.getUserId(),currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShip_requestRepo.delete(request.get());;
    }

    public void deleteFriend(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        Optional<FriendShip> request=friendShipRepo.findFriendShip(userId.getUserId(),currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShipRepo.delete(request.get());
    }

}
