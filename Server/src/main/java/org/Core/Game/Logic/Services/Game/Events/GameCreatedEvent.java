package org.Core.Game.Logic.Services.Game.Events;


import org.Core.Game.Logic.Api.Dto.GameFound;

public record GameCreatedEvent(
        GameFound forWhite,
        GameFound      forBlack,
        String whiteId,
        String    whiteSession,
        String blackId,
        String blackSession
) {
    public record UserSession(String userId, String sessionId) {}
}