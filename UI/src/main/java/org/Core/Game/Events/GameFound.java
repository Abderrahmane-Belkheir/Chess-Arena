package org.Core.Game.Events;

import com.github.bhlangonijr.chesslib.Side;
import lombok.Data;
@Data
public class GameFound {
    private boolean found;
    private String id;
    private Player opponent;
    private String fen;
    private Side mySide;
    @Data
    public static class Player{
        private int id;
        private String username;
        private int elo;
        private String avatarUrl;
    }
}
