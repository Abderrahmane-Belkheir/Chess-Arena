package org.Core.Game.History.Services;

import lombok.RequiredArgsConstructor;
import org.Core.Game.History.Api.Dto.GameDetails;
import org.Core.Game.History.Api.Dto.GameHistory;
import org.Core.Game.Logic.Services.Game.Events.GameOverInfo;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamesHistoryService {

    // TODO: fake data for testing the client — replace with a real query.
    public GameHistory fetchGames(){
        return new GameHistory(List.of(
                new GameHistory.GameEntry("MagnusCarlsen_2014", GameOverInfo.GameResult.WIN, 12, Instant.now().minusSeconds(3600)),
                new GameHistory.GameEntry("HikaruNakamura", GameOverInfo.GameResult.LOSS, -8, Instant.now().minusSeconds(7200)),
                new GameHistory.GameEntry("ChessMaster99", GameOverInfo.GameResult.DRAW, 3, Instant.now().minusSeconds(86400))
        ));
    }

    public GameDetails fetchGameDetails(String gameId){
        return null;
    }

    public List<String> fetchGameMoves(String gameId){
        return List.of();
    }

}
