package org.Core.GameLogic.Services.Game.Events;

import org.Core.GameLogic.Api.Dto.GameOverInfo;
import org.Core.GameLogic.Api.Dto.MoveConfirmation;

public record MoveConfirmationEvent(String userId, MoveConfirmation confirmation) {
}
