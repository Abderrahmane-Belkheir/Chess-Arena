package org.Core.GameLogic.Services.Game.Events;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = MoveResponse.class,    name = "MOVE"),
        @JsonSubTypes.Type(value = GameOverInfo.class,        name = "GAME_OVER"),
        @JsonSubTypes.Type(value = MoveConfirmation.class,     name = "MOVE_CONFIRM"),
        @JsonSubTypes.Type(value = DrawOfferEvent.class,     name = "DRAW_OFFERED"),
        @JsonSubTypes.Type(value = SpectatedResponse.class,name = "SPECTATE_REQUEST" )
})
public sealed abstract class GameEvent
        permits DrawOfferEvent, GameOverInfo, MoveConfirmation, MoveResponse, SpectatedResponse {
}