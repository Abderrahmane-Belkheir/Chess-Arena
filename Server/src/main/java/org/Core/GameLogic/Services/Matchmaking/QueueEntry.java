package org.Core.GameLogic.Services.Matchmaking;


import lombok.Getter;



public record QueueEntry(
    String userId,
    int publicId,
    String username,
    int    elo,
    String avatarUrl,
    long   joinedAt,
    String sessionId)
{}