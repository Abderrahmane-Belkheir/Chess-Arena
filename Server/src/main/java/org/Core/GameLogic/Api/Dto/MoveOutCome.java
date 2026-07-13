package org.Core.GameLogic.Api.Dto;

import org.Core.GameLogic.Services.Game.Events.GameOverInfo;
import org.Core.GameLogic.Services.Game.Events.MoveResponse;

public record MoveOutCome(boolean gameOver, String newFen, MoveResponse opponentPayload, GameOverInfo moverGameOverEvent) {
}
