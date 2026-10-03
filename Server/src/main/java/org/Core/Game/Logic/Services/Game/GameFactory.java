package org.Core.Game.Logic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.Game.Logic.Api.Dto.GameFound;
import org.Core.Game.Logic.Models.Color;
import org.Core.Game.Logic.Models.Game;
import org.Core.Game.Logic.Models.GameSession;
import org.Core.Game.Logic.Models.Player;
import org.Core.Game.Logic.Persistence.GameRepo;
import org.Core.Game.Logic.Services.Game.Events.GameCreatedEvent;
import org.Core.Game.Logic.Services.Game.Events.Id;
import org.Core.Game.Logic.Services.Matchmaking.MatchedPair;
import org.Core.Game.Logic.Services.Matchmaking.QueueEntry;
import org.Core.Game.Logic.Services.MoveValidation.GameSessionRegistry;
import org.Core.Game.Logic.Utilities;
import org.Core.Game.Social.PendingSpectateRequestStore;
import org.Core.Game.Social.SpectatorApprovalRegistry;
import org.Core.Scheduling.TimeOutSchedulingService;
import org.Core.Social.Api.Dto.FriendsList;
import org.Core.Social.Services.FriendStatusBroadcaster;
import org.Core.User.Models.User;
import org.Core.User.Persistence.UserRepo;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameFactory {


    private final PendingSpectateRequestStore pendingSpectateRequestStore;
    private final SpectatorApprovalRegistry spectatorApprovalRegistry;
    private final GameSessionStore gameSessionStore;
    private final GameSessionRegistry gameSessionRegistry;
    private final GameOverHandler gameOverHandler;
    private final TimeOutSchedulingService timeOutSchedulingService;
    private final GameRepo gameRepo;
    private final UserRepo userRepo;
    private final ApplicationEventPublisher eventPublisher;
    private final FriendStatusBroadcaster friendStatusBroadcaster;

    @Transactional
    public void createGame(MatchedPair matchedPair, Game.GameType type, Game.MatchMode matchMode) {
        GamePair gamePair = assignColorToPlayers(matchedPair);

        QueueEntry whiteQE = gamePair.whitePl();
        QueueEntry blackQE = gamePair.blackPl();

        String gameId = UUID.randomUUID().toString();
        String fen = Utilities.START_POSITION;

        log.debug("Assigned colors - White: {}, Black: {}",
                whiteQE.userId(),
                blackQE.userId());

        Game game = new Game(gameId, fen, type, matchMode);

        Player whitePl = new Player(Color.WHITE, userRepo.getReferenceById(whiteQE.userId()));
        Player blackPl = new Player(Color.BLACK, userRepo.getReferenceById(blackQE.userId()));

        game.players(whitePl, blackPl);

        gameRepo.save(game);

        gameSessionStore.save(
                gameId,
                new GameSession(
                        gameId,
                        type,
                        whiteQE.userId(),
                        whiteQE.publicId(),
                        blackQE.userId(),
                        blackQE.publicId(),
                        Color.WHITE,
                        true,
                        0,
                        0,
                        game.getCreatedAt()
                )
        );
        userRepo.updateUsersStatus(User.Status.IN_GAME,whiteQE.userId(),blackQE.userId());
        friendStatusBroadcaster.notifyStatusChange(whiteQE.userId(), whiteQE.publicId(), FriendsList.Status.InGame);
        friendStatusBroadcaster.notifyStatusChange(blackQE.userId(), blackQE.publicId(), FriendsList.Status.InGame);
        gameSessionRegistry.createSession(gameId, fen);
        long gameDuration=type== Game.GameType.RAPID?Utilities.TEN_MINUTES_MS:Utilities.THREE_MINUTES_MS;
        timeOutSchedulingService.schedule(gameId,gameDuration,()->gameOverHandler.handleTimeOut(gameId,new Id(blackQE.userId(),blackQE.publicId()),new Id(whiteQE.userId(),whiteQE.publicId()),Color.BLACK));
        pendingSpectateRequestStore.init(whiteQE.userId(),blackQE.userId());
        spectatorApprovalRegistry.init(whiteQE.publicId(),blackQE.publicId());
        eventPublisher.publishEvent(
                new GameCreatedEvent(
                        buildFor(blackQE, true, gameId, fen),
                        buildFor(whiteQE, false, gameId, fen),
                        whiteQE.userId(),
                        whiteQE.sessionId(),
                        blackQE.userId(),
                        blackQE.sessionId()
                )
        );

    }

    private GamePair assignColorToPlayers(MatchedPair pair){
        QueueEntry playerA=pair.playerA();
        QueueEntry playerB=pair.playerB();
        boolean playerAIsWhite = new Random().nextBoolean();
        QueueEntry whiteQE=playerAIsWhite?playerA:playerB;
        QueueEntry blackQE=playerAIsWhite?playerB:playerA;
        return new GamePair(whiteQE,blackQE);
    }

    private GameFound buildFor(QueueEntry opponent, boolean isWhite, String gameId, String fen) {
        return new GameFound(
                true,
                gameId,
                new GameFound.Player(
                        opponent.publicId(),
                        opponent.username(),
                        opponent.elo(),
                        opponent.avatarUrl()
                ),
                fen,
                isWhite ? Color.WHITE : Color.BLACK
        );
    }

    }

