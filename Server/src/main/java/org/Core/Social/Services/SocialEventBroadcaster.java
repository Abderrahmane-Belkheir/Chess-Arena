package org.Core.Social.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Social.Api.Dto.FriendsList;
import org.Core.Social.Api.Dto.InvitationsList;
import org.Core.Social.Events.Challenge;
import org.Core.Social.Events.FriendAdded;
import org.Core.Social.Events.FriendRemoved;
import org.Core.Social.Events.Invitation;
import org.Core.Social.Events.InvitationRemoved;
import org.Core.Social.Events.SocialEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pushes SocialEvents over /queue/social off its own thread pool rather than
 * the calling (request/transaction) thread, so callers like FriendShipManager
 * never wait on the push.
 */
@Service
@RequiredArgsConstructor
public class SocialEventBroadcaster {

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    private final SimpMessagingTemplate messagingTemplate;

    public void notifyInvitationReceived(String recipientUserId, InvitationsList.InvitationEntry entry) {
        send(recipientUserId, new Invitation(entry));
    }

    /**
     * Tells recipientUserId that the pending request involving publicId is
     * gone (the other side declined it or unsent it) — recipientUserId
     * should remove whichever row in their Requests list matches publicId.
     */
    public void notifyInvitationRemoved(String recipientUserId, int publicId) {
        send(recipientUserId, new InvitationRemoved(publicId));
    }

    /**
     * Tells recipientUserId that publicId removed the friendship between
     * them — recipientUserId should drop publicId from their friends list.
     */
    public void notifyFriendRemoved(String recipientUserId, int publicId) {
        send(recipientUserId, new FriendRemoved(publicId));
    }

    /**
     * Tells recipientUserId (the original sender of the accepted request)
     * that entry is now their friend — recipientUserId should insert entry
     * into whichever section matches entry.getStatus().
     */
    public void notifyFriendAdded(String recipientUserId, FriendsList.FriendEntry entry) {
        send(recipientUserId, new FriendAdded(entry));
    }

    /** Tells recipientUserId that challenge's sender wants to play them. */
    public void notifyChallenge(String recipientUserId, Challenge challenge) {
        send(recipientUserId, challenge);
    }

    private void send(String recipientUserId, SocialEvent event) {
        // Deferred to after commit — a caller's transaction rolling back
        // after this is queued shouldn't still result in the push going out.
        PostCommitRunner.runAfterCommit(() -> executor.submit(() -> messagingTemplate.convertAndSendToUser(
                recipientUserId, "/queue/social", event
        )));
    }

}
