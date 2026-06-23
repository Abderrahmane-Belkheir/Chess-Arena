package org.Core.Game.Events;

public class GameFound {
    private boolean isFound;
    private String id;
    private Opponent opponent;
    private String fen;

    @Override
    public String toString() {
        return "GameFound{" +
                "isFound=" + isFound +
                ", id='" + id + '\'' +
                ", opponent=" + opponent +
                ", fen='" + fen + '\'' +
                '}';
    }

    public static class Opponent{
        private int id;
        private String username;
        private int elo;
        private String avatarUrl;
    }

}
