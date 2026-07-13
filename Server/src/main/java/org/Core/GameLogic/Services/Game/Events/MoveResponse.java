package org.Core.GameLogic.Services.Game.Events;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class MoveResponse extends GameEvent{
       private String from;
       private  String to;
       private String newFen;
       private  GameOverInfo gameOverInfo;
}

