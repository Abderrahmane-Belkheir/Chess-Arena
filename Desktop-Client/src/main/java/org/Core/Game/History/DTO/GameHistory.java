package org.Core.Game.History.DTO;

import lombok.Data;
import org.Core.Config.DTO;
import org.Core.Game.Events.GameOverInfo;

import java.time.Instant;
import java.util.List;

@Data
public class GameHistory extends DTO {

    private List<GameEntry> games;

    @Data
    public static class GameEntry {
        private String name;
        private GameOverInfo.GameResult result;
        private int eloGained;
        private Instant date;
    }

}
