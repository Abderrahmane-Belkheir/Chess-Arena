package org.Core.Game.Events;

import lombok.Data;

@Data
public final class MoveConfirmation extends GameEvent{
    private String from;
    private String to;
    private String fen;
    private long myRemainingMs;
    private long oppRemainingMs;
    private GameOverInfo gameOverInfo;
}