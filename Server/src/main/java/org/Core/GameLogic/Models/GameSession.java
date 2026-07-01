package org.Core.GameLogic.Models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
public class GameSession {
    private String whitePlayerId;
    private String blackPlayerId;
    private Player.Color turn;
    private boolean active;
    private long    lastMoveAt;
}
