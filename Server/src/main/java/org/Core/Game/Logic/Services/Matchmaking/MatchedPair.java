package org.Core.Game.Logic.Services.Matchmaking;



public record MatchedPair(
     QueueEntry playerA,
        QueueEntry playerB
) {}