package org.Core.GameLogic.Services.Game.Events;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class MoveConfirmation extends GameEvent{
    private String from;
    private String to;
   private   String fen;
   private long myRemainingMs;
   private long oppRemainingMs;
   private GameOverInfo gameOverInfo;
}
