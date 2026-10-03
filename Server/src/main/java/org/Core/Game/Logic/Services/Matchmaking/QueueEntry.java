package org.Core.Game.Logic.Services.Matchmaking;





public record QueueEntry(
    String userId,
    int publicId,
    String username,
    int    elo,
    String avatarUrl,
    long   joinedAt,
    String sessionId)
{}