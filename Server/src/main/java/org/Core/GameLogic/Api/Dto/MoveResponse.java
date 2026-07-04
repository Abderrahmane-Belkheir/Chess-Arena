package org.Core.GameLogic.Api.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MoveResponse {
    private boolean gameOver;
    private GameResult result;
    private String from;
    private String to;
    private String newFen;
    public enum GameResult{CHECKMATE,STALEMATE,DRAW}
}
