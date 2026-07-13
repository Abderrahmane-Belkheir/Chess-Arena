package org.Core.Game.Events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public final class GameOverInfo extends GameEvent {
    private GameResult result;
    private EndReason endReason;
    private int newElo;
    public enum GameResult{WIN,LOSS,DRAW}
    public enum EndReason{CHECKMATE, RESIGNATION, TIMEOUT, STALEMATE, DRAW_AGREEMENT, INSUFFICIENT_MATERIAL, REPETITION, ABANDONED}
}
