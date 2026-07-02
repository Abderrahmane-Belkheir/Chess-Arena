package org.Core.GameLogic.Services.Matchmaking;
import lombok.RequiredArgsConstructor;
import org.Core.GameLogic.Models.Player;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class MatchmakingEntry {

    private final MatchmakingQueueService queueService;
    private final GameFactory gameFactory;

    // THIS IS A DEMO TEST
    public void searchGame(String userId) throws InterruptedException {

    }

    public Player.Color assignPreferedPlayerColor(String userId){
        return null;
    }

}
