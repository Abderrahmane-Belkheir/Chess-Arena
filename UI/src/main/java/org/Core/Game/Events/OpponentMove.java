package org.Core.Game.Events;

import lombok.Data;

@Data
public class OpponentMove {
    private GameOverInfo gameOverInfo;
    private String from;
    private String to;
    private String newFen;
}
