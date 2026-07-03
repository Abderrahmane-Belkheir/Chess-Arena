package org.Core.GameLogic.Api.Dto;

public record UserSession(
        String userId,
        String sessionId
) {}