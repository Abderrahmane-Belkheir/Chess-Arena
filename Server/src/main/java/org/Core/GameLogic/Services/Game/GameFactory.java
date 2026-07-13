package org.Core.GameLogic.Services.Game;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.Core.GameLogic.Api.Dto.GameFound;
import org.Core.GameLogic.Utilities;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Game;
import org.Core.GameLogic.Utilities.*;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Models.Player;
import org.Core.GameLogic.Persistence.GameRepo;
import org.Core.GameLogic.Services.Game.Events.GameCreatedEvent;
import org.Core.GameLogic.Services.Matchmaking.MatchedPair;
import org.Core.GameLogic.Services.Matchmaking.QueueEntry;
import org.Core.GameLogic.Services.MoveValidation.GameSessionRegistry;
import org.Core.Scheduling.TimeOutSchedulingService;
import org.Core.Social.Game.SpectatorApprovalRegistry;
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


    private final SpectatorApprovalRegistry spectatorApprovalRegistry;
    private final GameSessionStore gameSessionStore;
    private final GameSessionRegistry gameSessionRegistry;
    private final GameOverHandler gameOverHandler;
    private final TimeOutSchedulingService timeOutSchedulingService;
    private final GameRepo gameRepo;
    private final UserRepo userRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createGame(MatchedPair matchedPair,Game.GameType type) {
        GamePair gamePair = assignColorToPlayers(matchedPair);

        QueueEntry whiteQE = gamePair.whitePl();
        QueueEntry blackQE = gamePair.blackPl();

        String gameId = UUID.randomUUID().toString();
        String fen = Utilities.START_POSITION;

        log.debug("Assigned colors - White: {}, Black: {}",
                whiteQE.userId(),
                blackQE.userId());

        Game game = new Game(gameId, fen,type);

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
                        blackQE.userId(),
                        Color.WHITE,
                        true,
                        0,
                        0,
                        game.getCreatedAt()
                )
        );
        gameSessionRegistry.createSession(gameId, fen);
        long gameDuration=type== Game.GameType.RAPID?Utilities.TEN_MINUTES_MS:Utilities.THREE_MINUTES_MS;
        timeOutSchedulingService.schedule(gameId,gameDuration,()->gameOverHandler.handleTimeOut(gameId,blackQE.userId(),whiteQE.userId(),Color.BLACK));
        spectatorApprovalRegistry.init(whiteQE.userId(),blackQE.userId());
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

    private GameFound buildFor(QueueEntry opponent,boolean isWhite,String gameId,String fen) {
        return new GameFound(
                true,
                gameId,
                new GameFound.Opponent(
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

