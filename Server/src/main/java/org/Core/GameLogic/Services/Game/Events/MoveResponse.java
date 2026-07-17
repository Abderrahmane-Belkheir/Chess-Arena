package org.Core.GameLogic.Services.Game.Events;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.Core.GameLogic.Models.Color;

@Data
@AllArgsConstructor
public final class MoveResponse extends GameEvent{
    private String from;
    private  String to;
    private String newFen;
    private  GameOverInfo gameOverInfo;
}

