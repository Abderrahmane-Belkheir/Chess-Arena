package org.Core.GameLogic.Services.Matchmaking;

public record MatchedPair(
        QueueEntry playerA,
        QueueEntry playerB
) {}