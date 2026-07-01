package org.Core.GameLogic.Api.Dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bhlangonijr.chesslib.Side;
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
    @JsonProperty("mySide")
    private Side playerSide;


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


