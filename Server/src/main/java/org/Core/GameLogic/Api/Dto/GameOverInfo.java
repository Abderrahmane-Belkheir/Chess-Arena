package org.Core.GameLogic.Api.Dto;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameOverInfo {
    private GameResult result;
    private EndReason endReason;
    public enum GameResult{WIN,LOSS,DRAW}
    public enum EndReason{CHECKMATE, RESIGNATION, TIMEOUT, STALEMATE, DRAW_AGREEMENT, INSUFFICIENT_MATERIAL, REPETITION, ABANDONED,DRAW}
}
