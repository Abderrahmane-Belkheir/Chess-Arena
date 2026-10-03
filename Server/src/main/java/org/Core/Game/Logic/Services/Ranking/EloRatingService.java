package org.Core.Game.Logic.Services.Ranking;

import org.Core.Game.Logic.Services.Game.Events.GameOverInfo;
import org.springframework.stereotype.Service;

@Service
public class EloRatingService {

    /** Rates one player at a time — playerElo/opponentElo are that player's own perspective, result is that player's own outcome. */
    public GameOverInfo.EloSummary calculate(int playerElo, int opponentElo, GameOverInfo.GameResult result) {
        int change;
        if (result == GameOverInfo.GameResult.DRAW) {
            change = 3;
        } else if (playerElo < 1400) {
            change = result == GameOverInfo.GameResult.WIN ? 14 : -6;
        } else if (playerElo < 1800) {
            change = result == GameOverInfo.GameResult.WIN ? 12 : -8;
        } else {
            change = result == GameOverInfo.GameResult.WIN ? 10 : -10;
        }
        return new GameOverInfo.EloSummary(playerElo, change, playerElo + change);
    }

}
