package org.Core.Game.Events;

import com.github.bhlangonijr.chesslib.Square;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerMove {
    private String from;
    private String to;
    private String newFen;
}
