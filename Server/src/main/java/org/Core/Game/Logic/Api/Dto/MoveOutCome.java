package org.Core.Game.Logic.Api.Dto;

import org.Core.Game.Logic.Services.Game.Events.GameOverInfo;
import org.Core.Game.Logic.Services.Game.Events.MoveResponse;

;

public record MoveOutCome(boolean gameOver, String newFen, MoveResponse opponentPayload, GameOverInfo moverGameOverEvent) {
}
