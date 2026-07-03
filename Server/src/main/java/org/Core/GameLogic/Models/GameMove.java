package org.Core.GameLogic.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameMove {
    @Id
    @GeneratedValue
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    private Game game;

    @Enumerated(EnumType.STRING)
    private Color color;

    private Instant playedAt;

    private String from;
    private String to;
    private String fenAfter;

}
