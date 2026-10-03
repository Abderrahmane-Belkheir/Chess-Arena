package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsList;
import org.Core.Social.Events.FriendStatus;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.User.Models.User;
import org.Core.User.Services.PresenceStore;
import org.Core.User.Services.Events.UserCameOnlineEvent;
import org.Core.User.Services.Events.UserWentOfflineEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pushes a FriendStatus event to a user's online friends whenever that user's
 * status changes (e.g. entering/leaving a game). Runs off its own thread pool
 * rather than the calling (request/transaction) thread, so callers like
 * GameFactory.createGame never wait on the friend lookup + fan-out.
 */
@Service
@RequiredArgsConstructor
public class FriendStatusBroadcaster {

    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final FriendShipRepo friendShipRepo;
    private final PresenceStore presenceStore;
    private final SimpMessagingTemplate messagingTemplate;

    /** No real elo change to report — entering/leaving a game, coming online/offline, etc. */
    public void notifyStatusChange(String userId, int userPublicId,
                                     FriendsList.Status newStatus) {
        notifyStatusChange(userId, userPublicId, newStatus, 0);
    }

    /** newElo should only ever be a real value from GameOverHandler, once a ranked game actually changed it. */
    public void notifyStatusChange(String userId, int userPublicId,
                                     FriendsList.Status newStatus, int newElo) {
        // Deferred to after commit — a caller's transaction rolling back
        // after this is queued shouldn't still result in the push going out.
        PostCommitRunner.runAfterCommit(() -> executor.submit(() -> broadcast(userId, userPublicId, newStatus, newElo)));
    }

    @EventListener
    public void onUserCameOnline(UserCameOnlineEvent event) {
        notifyStatusChange(event.userId(), event.publicId(), FriendsList.Status.InLobby);
    }

    @EventListener
    public void onUserWentOffline(UserWentOfflineEvent event) {
        notifyStatusChange(event.userId(), event.publicId(), FriendsList.Status.Offline);
    }

    private void broadcast(String userId, int userPublicId,FriendsList.Status newStatus, int newElo) {
        List<User> friends = friendShipRepo.findFriendsOf(userId);
        if (friends.isEmpty()) return;

        Set<String> onlineIds = Set.copyOf(presenceStore.groupByPresence(
                friends.stream().map(User::getId).toList()
        ).get(true));

        if (onlineIds.isEmpty()) return;

        FriendStatus event = new FriendStatus(userPublicId, newStatus, newElo);

        friends.stream()
                .filter(friend -> onlineIds.contains(friend.getId()))
                .forEach(friend -> messagingTemplate.convertAndSendToUser(
                        friend.getId(), "/queue/social", event
                ));
    }
}
