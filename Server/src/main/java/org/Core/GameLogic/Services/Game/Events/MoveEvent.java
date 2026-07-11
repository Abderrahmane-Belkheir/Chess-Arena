package org.Core.GameLogic.Services.Game.Events;



import org.Core.GameLogic.Api.Dto.MoveResponse;

public record MoveEvent(String userId, MoveResponse response) {}


