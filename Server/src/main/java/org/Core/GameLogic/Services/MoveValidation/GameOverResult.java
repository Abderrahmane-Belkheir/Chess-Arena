package org.Core.GameLogic.Services.MoveValidation;

import org.Core.GameLogic.Services.Game.Events.GameOverInfo;

public record GameOverResult(boolean gameOver,GameOverInfo.GameResult result, GameOverInfo.EndReason endReason) {
}
