package org.Core.Game.Logic.Api.Dto;

public record UserSession(
        String userId,
        String sessionId
) {}