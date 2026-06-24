package org.Core.Matchmaking.Api.Dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameFound {
    private boolean found;
    private String id;
    private Opponent opponent;
    private String fen;
    private PlayerColor playerColor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public static class Opponent{
            private int id;
            private String username;
            private int elo;
            private String avatarUrl;
        }
    public enum PlayerColor{WHITE,BLACK}
    }


