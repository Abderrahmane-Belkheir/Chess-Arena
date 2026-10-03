package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsList;
import org.Core.Social.Api.Dto.InvitationsList;
import org.Core.Social.Models.FriendShip_Request;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Persistence.FriendShip_RequestRepo;

import org.Core.User.Models.User;
import org.Core.User.Services.AuthenticatedUserService;
import org.Core.User.Services.PresenceStore;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FriendShipQuery {

    private final FriendShipRepo friendShipRepo;
    private final FriendShip_RequestRepo friendShip_requestRepo;
    private final PresenceStore presenceStore;
    private final AuthenticatedUserService authenticatedUserService;

    public FriendsList getFriends(){
        String currentUserId = authenticatedUserService.getCurrentUser();
        List<User> friends = friendShipRepo.findFriendsOf(currentUserId);
        if(friends.isEmpty()) return new FriendsList(new HashMap<>());
        Set<String> onlineIds = Set.copyOf(presenceStore.groupByPresence(
                friends.stream().map(User::getId).toList()
        ).get(true));

        Map<FriendsList.Status, List<FriendsList.FriendEntry>> grouped = new EnumMap<>(FriendsList.Status.class);
        for (FriendsList.Status status : FriendsList.Status.values()) {
            grouped.put(status, new ArrayList<>());
        }

        for (User friend : friends) {
            grouped.get(resolveStatus(friend, onlineIds)).add(new FriendsList.FriendEntry(
                    friend.getPublicId(),
                    friend.getUsername(),
                    friend.getElo(),
                    friend.getAvatarUrl(),
                    null
            ));
        }

        return new FriendsList(grouped);
    }

    private FriendsList.Status resolveStatus(User friend, Set<String> onlineIds) {
        if (!onlineIds.contains(friend.getId())) return FriendsList.Status.Offline;
        return friend.getStatus() == User.Status.IN_GAME ? FriendsList.Status.InGame : FriendsList.Status.InLobby;
    }



    public InvitationsList getInvitations(){
        String currentUserId = authenticatedUserService.getCurrentUser();
        List<FriendShip_Request> requests = friendShip_requestRepo.findInvitationsOf(currentUserId);
        if (requests.isEmpty()) return new InvitationsList(new ArrayList<>());

        List<InvitationsList.InvitationEntry> entries = requests.stream()
                .map(request -> {
                    boolean incoming = request.getRecipient().getId().equals(currentUserId);
                    User other = incoming ? request.getSender() : request.getRecipient();
                    return new InvitationsList.InvitationEntry(
                            other.getUsername(),
                            String.valueOf(other.getPublicId()),
                            other.getElo(),
                            other.getAvatarUrl(),
                            null,
                            incoming
                    );
                })
                .toList();

        return new InvitationsList(entries);
    }

}
