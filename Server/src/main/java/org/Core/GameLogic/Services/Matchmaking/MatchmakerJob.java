package org.Core.GameLogic.Services.Matchmaking;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Services.Game.GameFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchmakerJob {

    private final MatchmakingQueueService queueService;
    private final GameFactory gameFactory;

}
