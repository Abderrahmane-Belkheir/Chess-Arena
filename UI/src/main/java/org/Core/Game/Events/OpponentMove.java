package org.Core.Game.Events;

import lombok.Data;

@Data
public final class OpponentMove extends GameEvent {
    private String from;
    private String to;
    private String newFen;
    private GameOverInfo gameOverInfo;
}

