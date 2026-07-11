package org.Core.GameLogic.Api.Dto;

import org.Core.GameLogic.Services.Game.Events.GameOverEvent;

public record MoveOutCome(boolean gameOver, String newFen, MoveResponse opponentPayload, GameOverInfo moverGameOverEvent) {
}
