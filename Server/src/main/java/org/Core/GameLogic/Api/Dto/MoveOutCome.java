package org.Core.GameLogic.Api.Dto;

public record MoveOutCome(boolean gameOver,MoveResponse opponentPayload,GameOverInfo moverGameOverInfo) {
}
