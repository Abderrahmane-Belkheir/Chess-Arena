package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsList;
import org.Core.Social.Api.Dto.InvitationsList;
import org.Core.Social.Exceptions.InvitationRequestException;
import org.Core.Social.Models.FriendShip;
import org.Core.Social.Models.FriendShip_Request;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Persistence.FriendShip_RequestRepo;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.Core.User.Services.AuthenticatedUserService;
import org.Core.User.Services.PresenceStore;
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
    private final SocialEventBroadcaster socialEventBroadcaster;
    private final PresenceStore presenceStore;


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

        // Recipient sees it as incoming (from currentUser)...
        socialEventBroadcaster.notifyInvitationReceived(
                userId.getUserId(),
                new InvitationsList.InvitationEntry(
                        currentUser.getUsername(),
                        String.valueOf(currentUser.getPublicId()),
                        currentUser.getElo(),
                        currentUser.getAvatarUrl(),
                        null,
                        true
                )
        );
        // ...and the sender sees it appear in their own Requests tab too,
        // live, as outgoing (to recipientUser) — without this they'd only
        // see their own new request after a full reload.
        socialEventBroadcaster.notifyInvitationReceived(
                currentUserId,
                new InvitationsList.InvitationEntry(
                        recipientUser.getUsername(),
                        String.valueOf(recipientUser.getPublicId()),
                        recipientUser.getElo(),
                        recipientUser.getAvatarUrl(),
                        null,
                        false
                )
        );
    }

    public void unSend(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        friendShip_requestRepo.
                findBySenderIdAndRecipientId(currentUserId,userId.getUserId()).
                ifPresent(friendShipRequest -> {
                    friendShip_requestRepo.delete(friendShipRequest);
                    int currentUserPublicId=userRepo.getReferenceById(currentUserId).getPublicId();
                    socialEventBroadcaster.notifyInvitationRemoved(userId.getUserId(), currentUserPublicId);
                });
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
        // The original sender doesn't know we accepted yet — tell them so
        // their client can add currentUser as a friend live. currentUser is
        // online right now (they're the one who just clicked Accept), so
        // InLobby is the only status that makes sense here.
        socialEventBroadcaster.notifyFriendAdded(
                userId.getUserId(),
                new FriendsList.FriendEntry(
                        currentUser.getPublicId(),
                        currentUser.getUsername(),
                        currentUser.getElo(),
                        currentUser.getAvatarUrl(),
                        null,
                        FriendsList.Status.InLobby
                )
        );
        // ...and currentUser (the acceptor) sees recipientUser appear in
        // their own friends list too, live, with their actual current
        // status — without this only the sender's side would update.
        socialEventBroadcaster.notifyFriendAdded(
                currentUserId,
                new FriendsList.FriendEntry(
                        recipientUser.getPublicId(),
                        recipientUser.getUsername(),
                        recipientUser.getElo(),
                        recipientUser.getAvatarUrl(),
                        null,
                        resolveStatus(recipientUser)
                )
        );
    }

    private FriendsList.Status resolveStatus(User user) {
        if (!presenceStore.isOnline(user.getId())) return FriendsList.Status.Offline;
        return user.getStatus() == User.Status.IN_GAME ? FriendsList.Status.InGame : FriendsList.Status.InLobby;
    }

    public void reject(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        if(friendShipRepo.doesFriendShipExists(currentUserId,userId.getUserId())) return;
        Optional<FriendShip_Request> request=friendShip_requestRepo.findBySenderIdAndRecipientId(userId.getUserId(),currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShip_requestRepo.delete(request.get());
        int currentUserPublicId=userRepo.getReferenceById(currentUserId).getPublicId();
        socialEventBroadcaster.notifyInvitationRemoved(userId.getUserId(), currentUserPublicId);
    }

    public void deleteFriend(int publicId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        UserRepo.internalId userId=userRepo.getInternalId(publicId);
        if(userId.getUserId().equals(currentUserId)) return;
        Optional<FriendShip> request=friendShipRepo.findFriendShip(userId.getUserId(),currentUserId);
        if(request.isEmpty()) throw new RuntimeException();
        friendShipRepo.delete(request.get());
        int currentUserPublicId=userRepo.getReferenceById(currentUserId).getPublicId();
        socialEventBroadcaster.notifyFriendRemoved(userId.getUserId(), currentUserPublicId);
    }

}
