package org.Core.Game.Events;

import lombok.Data;

@Data
public final class MoveConfirmation extends GameEvent{
        String fen;
        long myRemainingMs;
        long oppRemainingMs;
        GameOverInfo gameOverInfo;
}