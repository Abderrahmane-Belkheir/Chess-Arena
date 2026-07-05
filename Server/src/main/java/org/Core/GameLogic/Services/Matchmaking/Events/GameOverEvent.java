package org.Core.GameLogic.Services.Matchmaking.Events;

import org.Core.GameLogic.Api.Dto.GameOverInfo;

public record GameOverEvent(String userId, GameOverInfo gameOverInfo) {
}
