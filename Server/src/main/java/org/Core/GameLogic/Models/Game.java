package org.Core.GameLogic.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.Core.GameLogic.Api.Dto.GameOverInfo;

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

    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    private GameStatus status=GameStatus.RUNNING;

    @Enumerated(EnumType.STRING)
    private Result result=Result.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    private GameOverInfo.EndReason endReason;

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

    public enum GameStatus{ENDED,RUNNING}
    public enum Result{IN_PROGRESS,WHITE_WIN,BLACK_WIN,DRAW}

}
