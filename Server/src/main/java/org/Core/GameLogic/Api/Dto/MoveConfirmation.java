package org.Core.GameLogic.Api.Dto;

public record MoveConfirmation(
        String fen,
        long myRemainingMs,
        long oppRemainingMs,
        GameOverInfo gameOverInfo
) {}