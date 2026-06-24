package org.Core.Game.Events;

import lombok.Data;
@Data
public class GameFound {
    private boolean found;
    private String id;
    private Opponent opponent;
    private String fen;
    private PlayerColor playerColor;
    @Data
    public static class Opponent{
        private int id;
        private String username;
        private int elo;
        private String avatarUrl;
    }

    public enum PlayerColor{WHITE,BLACK}
}
