package org.Core.Game.Logic.Services.Game;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.Core.Game.Logic.Models.Color;


import java.time.Instant;
@NoArgsConstructor
@Data
public class DrawOffer {
    private Color offeredBy;
    private Instant drawTime;
    public DrawOffer(Color offeredBy, Instant now) {
        this.offeredBy=offeredBy;
        this.drawTime=now;
    }
}
