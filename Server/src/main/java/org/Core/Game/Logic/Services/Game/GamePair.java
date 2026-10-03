package org.Core.Game.Logic.Services.Game;


import org.Core.Game.Logic.Services.Matchmaking.QueueEntry;

public record GamePair(QueueEntry whitePl, QueueEntry blackPl) {
}
