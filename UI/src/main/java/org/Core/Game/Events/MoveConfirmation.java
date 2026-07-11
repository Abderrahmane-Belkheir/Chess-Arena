package org.Core.Game.Events;

public record MoveConfirmation(
        String fen,
        long myRemainingMs,
        long oppRemainingMs,
        GameOverInfo gameOverInfo
) {}