package org.Core.GameLogic.Services.Matchmaking;





public record QueueEntry(
    String userId,
    int publicId,
    String username,
    int    elo,
    String avatarUrl,
    long   joinedAt,
    String sessionId)
{}