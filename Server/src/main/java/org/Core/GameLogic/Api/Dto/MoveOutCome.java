package org.Core.GameLogic.Api.Dto;

public record MoveOutCome(boolean gameOver,String newFen,MoveResponse opponentPayload,GameOverInfo moverGameOverInfo) {
}
