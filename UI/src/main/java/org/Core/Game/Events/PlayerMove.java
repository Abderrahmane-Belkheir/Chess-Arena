package org.Core.Game.Events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerMove {
    private String gameId;
    private String from;
    private String to;
}
