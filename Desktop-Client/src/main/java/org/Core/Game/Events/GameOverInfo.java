package org.Core.Game.Events;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public final class GameOverInfo extends GameEvent {
    private GameResult result;
    private EndReason endReason;
    // null for friendly games — rating doesn't change for those.
    private EloSummary eloSummary;
    public enum GameResult{WIN,LOSS,DRAW}
    public enum EndReason{CHECKMATE, RESIGNATION, TIMEOUT, STALEMATE, DRAW_AGREEMENT, INSUFFICIENT_MATERIAL, REPETITION, ABANDONED, DRAW}

    /** This recipient's own elo — what it was before the game, what this game changed it by (positive or negative), and what it is now. */
    public record EloSummary(int previousElo, int change, int newElo){}
}
