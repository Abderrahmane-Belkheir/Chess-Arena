package org.Core.GameLogic.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class GameSession {
    private String gameId;
    private Game.GameType type;
    private String whitePlayerId;
    private int whitePlayerPublicId;
    private String blackPlayerId;
    private int blackPlayerPublicId;
    private Color turn;
    private boolean active;
    private long whitePlayedTime;
    private long blackPlayedTime;
    private Instant lastMoveAt;
}
