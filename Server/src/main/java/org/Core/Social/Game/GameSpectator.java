package org.Core.Social.Game;

import com.github.bhlangonijr.chesslib.Side;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameFound;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Models.Player;
import org.Core.GameLogic.Services.Game.Events.Event;
import org.Core.GameLogic.Services.Game.Events.Id;
import org.Core.GameLogic.Services.Game.Events.SpectatedResponse;
import org.Core.GameLogic.Services.Game.Events.SpectatorResponse;
import org.Core.GameLogic.Services.Game.GameSessionStore;
import org.Core.GameLogic.Services.MoveValidation.GameSessionRegistry;
import org.Core.GameLogic.Utilities;
import org.Core.Social.Persistence.FriendShipRepo;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class GameSpectator {

    private final FriendShipRepo friendShipRepo;
    private final SpectatorApprovalRegistry approvalRegistry;
    private final PendingSpectateRequestStore spectateRequestStore;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepo userRepo;
    private final GameSessionStore gameSessionStore;
    private final GameSessionRegistry gameSessionRegistry;

    @Transactional(readOnly = true)
    public void requestSpectate(String userId,int targetId){
        if(approvalRegistry.isApproved(targetId,userId)) return;
        User user=userRepo.findById(userId).orElseThrow();
        UserRepo.internalId internalId =userRepo.getInternalId(targetId);
        String internalTargetId=internalId.getUserId();
        if(internalTargetId.equals(userId)) throw new RuntimeException();
        if(!friendShipRepo.doesFriendShipExists(userId, internalTargetId)) return;
        if(!spectateRequestStore.create(user.getPublicId(), internalTargetId)) return;
        eventPublisher.publishEvent(new Event(new Id(internalTargetId,0),new SpectatedResponse(user.getPublicId(),user.getUsername(),user.getAvatarUrl())));
    }

    public void acceptSpectate(String userId, int spectatorId) {
        if (!spectateRequestStore.resolve(spectatorId, userId)) { return; }
        UserRepo.internalId internalId = userRepo.getInternalId(spectatorId);
        Optional<String> gameId = gameSessionStore.findGame(userId);
        if (gameId.isEmpty()) return;

        GameSession session = gameSessionStore.find(gameId.get()).orElse(null);
        if (session == null) return;

        Color side = Objects.equals(session.getWhitePlayerId(), userId) ? Color.WHITE : Color.BLACK;
        String opponentId = side == Color.WHITE ? session.getBlackPlayerId() : session.getWhitePlayerId();

        GameFound.Player spectated = createPlayer(userId);
        GameFound.Player opponent  = createPlayer(opponentId);

        long spectatedPlayerTimeMs = side == Color.WHITE ? session.getWhitePlayedTime() : session.getBlackPlayedTime();
        long opponentPlayedTimeMs  = side == Color.WHITE ? session.getBlackPlayedTime() : session.getWhitePlayedTime();

        if (session.getTurn() == side) {
            spectatedPlayerTimeMs += Duration.between(session.getLastMoveAt(), Instant.now()).toMillis();
        } else {
            opponentPlayedTimeMs += Duration.between(session.getLastMoveAt(), Instant.now()).toMillis();
        }
        long gameDuration=session.getType()== Game.GameType.RAPID?Utilities.TEN_MINUTES_MS:Utilities.THREE_MINUTES_MS;
        String fen=gameSessionRegistry.getFen(gameId.get()).orElse(null);
        approvalRegistry.approve(spectated.getId(),internalId.getUserId());
        eventPublisher.publishEvent(
                new SpectatorResponse(internalId.getUserId(), spectated, opponent,
                        gameDuration-spectatedPlayerTimeMs, gameDuration-opponentPlayedTimeMs,
                        side==Color.WHITE?Side.WHITE:Side.BLACK,session.getTurn()==Color.WHITE?Side.WHITE:Side.BLACK,fen)
        );
    }

    public boolean isApproved(int targetId,String spectatorId){
        return approvalRegistry.isApproved(targetId,spectatorId);
    }

    private GameFound.Player createPlayer(String id){
        User user=userRepo.findById(id).orElseThrow();
        return new GameFound.Player(user.getPublicId(),user.getUsername(),user.getElo(),user.getAvatarUrl());
    }

}
