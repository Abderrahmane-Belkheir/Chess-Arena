package org.Core.Game.Logic.Services.Game;

import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.Game.Logic.Models.Color;
import org.Core.Game.Logic.Models.Game;
import org.Core.Game.Logic.Persistence.GameRepo;
import org.Core.Game.Logic.Services.Game.Events.Event;
import org.Core.Game.Logic.Services.Game.Events.GameOverEvent;
import org.Core.Game.Logic.Services.Game.Events.GameOverInfo;
import org.Core.Game.Logic.Services.Game.Events.Id;
import org.Core.Game.Logic.Services.MoveValidation.GameSessionRegistry;
import org.Core.Game.Logic.Services.Ranking.EloSummaries;
import org.Core.Game.Logic.Services.Ranking.EloRatingService;
import org.Core.Game.Social.PendingSpectateRequestStore;
import org.Core.Game.Social.SpectatorApprovalRegistry;
import org.Core.Scheduling.TimeOutSchedulingService;
import org.Core.Social.Api.Dto.FriendsList;
import org.Core.Social.Services.FriendStatusBroadcaster;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GameOverHandler {

    private final ApplicationEventPublisher eventPublisher;
    private final GameRepo gameRepo;
    private final GameSessionStore gameSessionStore;
    private final GameSessionRegistry gameSessionRegistry;
    private final UserRepo userRepo;
    private final FriendStatusBroadcaster friendStatusBroadcaster;
    private final PendingSpectateRequestStore pendingSpectateRequestStore;
    private final SpectatorApprovalRegistry spectatorApprovalRegistry;
    private final EloRatingService eloRatingService;
    private final TimeOutSchedulingService timeOutSchedulingService;


    /** playerA/playerB convention: winner-then-loser when endReason isn't a draw (ordering doesn't matter for draws). */
    public  Optional<EloSummaries> handle(String gameId, String playerA, String playerB, Color winnerColor, GameOverInfo.EndReason endReason) {

            boolean isDraw=endReason== GameOverInfo.EndReason.DRAW||endReason== GameOverInfo.EndReason.DRAW_AGREEMENT;
            Game.Result result=isDraw? Game.Result.DRAW: winnerColor ==Color.WHITE? Game.Result.WHITE_WIN: Game.Result.BLACK_WIN;
            return endGame(new EndGame(gameId,playerA,playerB,result,endReason));
}

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleTimeOut(String gameId, Id winnerId, Id loserId, Color winnerColor){
        Game.Result result= winnerColor==Color.WHITE?Game.Result.WHITE_WIN:Game.Result.BLACK_WIN;
        Optional<EloSummaries> eloSummaries=endGame(new EndGame(gameId,winnerId.internalId(),loserId.internalId(),result, GameOverInfo.EndReason.TIMEOUT));
        GameOverInfo winner=new GameOverInfo(GameOverInfo.GameResult.WIN, GameOverInfo.EndReason.TIMEOUT, eloSummaries.map(EloSummaries::playerA).orElse(null));
        GameOverInfo looser=new GameOverInfo(GameOverInfo.GameResult.LOSS, GameOverInfo.EndReason.TIMEOUT, eloSummaries.map(EloSummaries::playerB).orElse(null));
        eventPublisher.publishEvent(new GameOverEvent(new Event(loserId,looser),new Event(winnerId,winner)));
    }

    private Optional<EloSummaries>  endGame(EndGame endGame){
        // Every path that ends a game needs this — cancel here once instead
        // of every caller remembering to do it before calling handle().
        timeOutSchedulingService.cancel(endGame.gameId);
        gameRepo.endGame(endGame.gameId, endGame.result,Game.GameStatus.ENDED,endGame.endReason, Instant.now());

        Game.MatchMode matchMode = gameRepo.findById(endGame.gameId)
                .map(Game::getMatchMode)
                .orElse(Game.MatchMode.RANKED);
        Optional<EloSummaries> eloSummaries=Optional.empty();
        if (matchMode == Game.MatchMode.RANKED) {
            User userA = userRepo.getReferenceById(endGame.playerA);
            User userB = userRepo.getReferenceById(endGame.playerB);
            boolean isDraw = endGame.result == Game.Result.DRAW;
            // playerA is the winner (see handle()'s contract) when it isn't a draw.
            GameOverInfo.GameResult resultA = isDraw ? GameOverInfo.GameResult.DRAW : GameOverInfo.GameResult.WIN;
            GameOverInfo.GameResult resultB = isDraw ? GameOverInfo.GameResult.DRAW : GameOverInfo.GameResult.LOSS;
            GameOverInfo.EloSummary summaryA = eloRatingService.calculate(userA.getElo(), userB.getElo(), resultA);
            GameOverInfo.EloSummary summaryB = eloRatingService.calculate(userB.getElo(), userA.getElo(), resultB);
            if (summaryA != null && summaryB != null) {
                eloSummaries = Optional.of(new EloSummaries(summaryA, summaryB));
                userRepo.updateUsersStatusAndElo(User.Status.IN_LOBBY,
                        endGame.playerA, summaryA.newElo(),
                        endGame.playerB, summaryB.newElo());
                notifyBackToLobby(endGame.playerA, summaryA.newElo());
                notifyBackToLobby(endGame.playerB, summaryB.newElo());
            }else{
                userRepo.updateUsersStatus(User.Status.IN_LOBBY,endGame.playerA, endGame.playerB);
                notifyBackToLobby(endGame.playerA);
                notifyBackToLobby(endGame.playerB);
            }
        }else {
            userRepo.updateUsersStatus(User.Status.IN_LOBBY,endGame.playerA, endGame.playerB);
            notifyBackToLobby(endGame.playerA);
            notifyBackToLobby(endGame.playerB);
        }
        gameSessionRegistry.removeSession(endGame.gameId);
        gameSessionStore.remove(endGame.gameId, endGame.playerA, endGame.playerB);
        clearSpectating(endGame.playerA);
        clearSpectating(endGame.playerB);

        return eloSummaries;
    }

    private void notifyBackToLobby(String userId) {
        userRepo.findById(userId).ifPresent(user ->
                friendStatusBroadcaster.notifyStatusChange(userId, user.getPublicId(), FriendsList.Status.InLobby));
    }

    /** newElo is a real value here — only called once a ranked game actually changed this user's rating. */
    private void notifyBackToLobby(String userId, int newElo) {
        userRepo.findById(userId).ifPresent(user ->
                friendStatusBroadcaster.notifyStatusChange(userId, user.getPublicId(), FriendsList.Status.InLobby, newElo));
    }

    // Pending spectate requests and approvals were scoped to this player's
    // now-finished game — nothing left to accept/reject or stay approved for.
    private void clearSpectating(String userId) {
        pendingSpectateRequestStore.clearForTarget(userId);
        userRepo.findById(userId).ifPresent(user -> spectatorApprovalRegistry.clearForTarget(user.getPublicId()));
    }

    record EndGame(String gameId,String playerA,String playerB ,Game.Result result, GameOverInfo.EndReason endReason){}




}