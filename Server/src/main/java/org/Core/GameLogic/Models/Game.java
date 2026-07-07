package org.Core.GameLogic.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@Entity
@Data
@NoArgsConstructor
public class Game  {

    @Id
    private String id;

    private Instant createdAt=Instant.now();

    private GameStatus status=GameStatus.RUNNING;

    @OneToMany(cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Player> players=new ArrayList<>();


    private String fen;

    public Game(String id,String fen){
        this.id=id;
        this.fen=fen;
    }

    public void players(Player white,Player black){
        this.players.addAll(List.of(white,black));
        white.setGame(this);
        black.setGame(this);
    }
    public enum GameStatus{FINISHED,RUNNING}
}
