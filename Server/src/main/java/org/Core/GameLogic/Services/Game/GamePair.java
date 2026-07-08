package org.Core.GameLogic.Services.Game;

import org.Core.GameLogic.Services.Matchmaking.QueueEntry;

public record GamePair(QueueEntry whitePl, QueueEntry blackPl) {
}
