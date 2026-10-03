package org.Core.Game.History.Api.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.Core.Game.Logic.Services.Game.Events.GameOverInfo;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameHistory {

    private List<GameEntry> games;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GameEntry {
        private String name;
        private GameOverInfo.GameResult result;
        private int eloGained;
        private Instant date;
    }

}
