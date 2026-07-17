package org.Core.GameLogic.Api.Dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.bhlangonijr.chesslib.Side;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.Core.GameLogic.Models.Color;
import org.Core.GameLogic.Models.Player;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameFound {
    private boolean found;
    private String id;
    private Player opponent;
    private String fen;
    @JsonProperty("mySide")
    private Color playerSide;


@AllArgsConstructor
@NoArgsConstructor
@Data
public static class Player{
            private int id;
            private String username;
            private int elo;
            private String avatarUrl;
        }
    }


