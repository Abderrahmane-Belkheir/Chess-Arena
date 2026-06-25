package org.Core.Game.Events;

import lombok.Data;

@Data
public class OpponentMove {
    private String from;
    private String to;
    private String newFen;
}
