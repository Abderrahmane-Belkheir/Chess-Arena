package org.Core.Game.Social;

import lombok.RequiredArgsConstructor;
import org.Core.Game.Logic.Models.Game;
import org.Core.Game.Logic.Services.Game.GameFactory;
import org.Core.Game.Logic.Services.Matchmaking.MatchedPair;
import org.Core.Game.Logic.Services.Matchmaking.QueueEntry;
import org.Core.Social.Events.Challenge;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.Social.Services.SocialEventBroadcaster;
import org.Core.User.Api.Controllers.PresenceController;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.Core.User.Services.AuthenticatedUserService;
import org.Core.User.Services.PresenceStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeService {

    private final AuthenticatedUserService authenticatedUserService;
    private final ChallengeRequestStore challengeRequestStore;
    private final UserRepo userRepo;
    private final FriendShipRepo friendShipRepo;
    private final SocialEventBroadcaster socialEventBroadcaster;
    private final GameFactory gameFactory;
    private final PresenceStore presenceStore;

    public void challenge(int publicId){
        String currentUserId = authenticatedUserService.getCurrentUser();
        UserRepo.internalId targetId = userRepo.getInternalId(publicId);
        if (!friendShipRepo.doesFriendShipExists(currentUserId, targetId.getUserId())) return;

        User currentUser = userRepo.getReferenceById(currentUserId);
        User targetUser = userRepo.getReferenceById(targetId.getUserId());
        if (!presenceStore.isOnline(targetId.getUserId()) || currentUser.getStatus() == User.Status.IN_GAME || targetUser.getStatus() == User.Status.IN_GAME) return;

        if (challengeRequestStore.create(currentUser.getPublicId(), targetId.getUserId())) {
            socialEventBroadcaster.notifyChallenge(
                    targetId.getUserId(),
                    new Challenge(currentUser.getPublicId(), currentUser.getUsername(),
                            currentUser.getElo(), currentUser.getAvatarUrl())
            );
        }
    }

    public void accept(int publicId){
        String currentUserId = authenticatedUserService.getCurrentUser();
        String targetUserId = challengeRequestStore.peek(publicId);
        if (targetUserId == null || !targetUserId.equals(currentUserId)) return;

        challengeRequestStore.resolve(publicId);

        UserRepo.internalId challengerId = userRepo.getInternalId(publicId);
        User challenger = userRepo.getReferenceById(challengerId.getUserId());
        User accepter = userRepo.getReferenceById(currentUserId);
        // Either side may have joined a different game since the challenge
        // was sent — re-checking here (not just at challenge()) is what
        // actually prevents a second GameFound for someone already playing,
        // rather than relying on client-side subscription timing for that.
        if (!presenceStore.isOnline(targetUserId)||challenger.getStatus() == User.Status.IN_GAME || accepter.getStatus() == User.Status.IN_GAME) return;

        // No STOMP session available here (this is a REST call, not a
        // /app/... message), so sessionId is left null — GameBroadcaster's
        // convertAndSendToUser still delivers to all of the user's active
        // sessions without it, same as every other broadcaster in this app.
        QueueEntry challengerEntry = new QueueEntry(challenger.getId(), challenger.getPublicId(),
                challenger.getUsername(), challenger.getElo(), challenger.getAvatarUrl(),
                System.currentTimeMillis(), null);
        QueueEntry accepterEntry = new QueueEntry(accepter.getId(), accepter.getPublicId(),
                accepter.getUsername(), accepter.getElo(), accepter.getAvatarUrl(),
                System.currentTimeMillis(), null);

        gameFactory.createGame(new MatchedPair(challengerEntry, accepterEntry), Game.GameType.RAPID, Game.MatchMode.FRIENDLY);
    }

    public void reject(int publicId){
        String currentUserId = authenticatedUserService.getCurrentUser();
        String targetUserId = challengeRequestStore.peek(publicId);
        if (targetUserId == null || !targetUserId.equals(currentUserId)) return;

        challengeRequestStore.resolve(publicId);
    }

}
