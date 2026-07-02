package org.Core.GameLogic.Services.Matchmaking;

import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Models.GameSession;
import org.Core.GameLogic.Models.Player;
import org.Core.GameLogic.Services.Authorazation.GameSessionStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GameFactory {

    private final GameSessionStore gameSessionStore;
    private final ApplicationEventPublisher eventPublisher;

    public void createGame(){


        eventPublisher.publishEvent(null);
    }

}
