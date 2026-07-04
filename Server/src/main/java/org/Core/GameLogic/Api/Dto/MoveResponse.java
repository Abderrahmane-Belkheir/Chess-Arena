package org.Core.GameLogic.Api.Dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MoveResponse {
    private String from;
    private String to;
    private String newFen;
}
