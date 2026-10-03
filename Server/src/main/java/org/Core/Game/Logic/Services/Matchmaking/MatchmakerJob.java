package org.Core.Game.Logic.Services.Matchmaking;

import lombok.RequiredArgsConstructor;
import org.Core.Game.Logic.Services.Game.GameFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchmakerJob {

    private final MatchmakingQueueService queueService;
    private final GameFactory gameFactory;

}
