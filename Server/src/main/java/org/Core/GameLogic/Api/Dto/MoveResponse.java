package org.Core.GameLogic.Api.Dto;


public record MoveResponse(
        String from,
        String to,
        String newFen,
        GameOverInfo gameOverInfo){}

