package org.Core.Game.Logic.Services.MoveValidation;


import org.Core.Game.Logic.Services.Game.Events.GameOverInfo;

public record GameOverResult(boolean gameOver, GameOverInfo.GameResult result, GameOverInfo.EndReason endReason) {
}
