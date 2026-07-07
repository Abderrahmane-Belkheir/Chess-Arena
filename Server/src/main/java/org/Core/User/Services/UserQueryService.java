package org.Core.User.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Persistence.FriendShip_RequestRepo;
import org.Core.User.Api.Dto.UserSummary;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepo userRepo;
    private final FriendShipRepo friendShipRepo;
    private final FriendShip_RequestRepo friendShip_requestRepo;
    private final AuthenticatedUserService authenticatedUserService;

    public UserSummary search(int userId){
        String currentUserId=authenticatedUserService.getCurrentUser();
        Optional<User> optionalUser =userRepo.findByPublicId(userId);
        if(optionalUser.isEmpty()) return new UserSummary();
        User user=optionalUser.get();
        UserSummary.UserSummaryBuilder userSummary=UserSummary.builder().id(userId).elo(user.getElo()).avatarUrl(user.getAvatarUrl()).username(user.getUsername());
        if(friendShipRepo.doesFriendShipExists(currentUserId,user.getId())) userSummary.isFriend(true);
        else {
            userSummary.isFriend(false);
            if(friendShip_requestRepo.existsBySenderIdAndRecipientId(currentUserId,user.getId())) userSummary.invitationStatus(UserSummary.InvitationStatus.SENT);
            else if (friendShip_requestRepo.existsBySenderIdAndRecipientId(user.getId(),currentUserId)) userSummary.invitationStatus(UserSummary.InvitationStatus.RECEIVED);
        }
        return userSummary.build();
    }

    public UserSummary getMyProfile(String currentUserId){
        User user= userRepo.findById(currentUserId).orElseThrow();
        return UserSummary.builder().id(user.getPublicId()).username(user.getUsername()).elo(user.getElo()).avatarUrl(user.getAvatarUrl()).build();
    }

}
