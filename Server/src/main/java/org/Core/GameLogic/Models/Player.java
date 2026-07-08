package org.Core.GameLogic.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.Core.User.Models.User;

@Entity
@Data
@NoArgsConstructor
public class Player {

    @Id
    @GeneratedValue
    private String id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "game_id")
    private Game game;

    @Enumerated(EnumType.STRING)
    private Color playerColor;

    private long timePlayed;

    private int eloGained;

    public Player(Color color,User user){
        this.playerColor=color;
        this.user=user;
    }
}
