
package org.Core.GameLogic.Api.Dto;



public record GameOverResult (
    GameOverInfo moverInfo,
    GameOverInfo opponentInfo
){}