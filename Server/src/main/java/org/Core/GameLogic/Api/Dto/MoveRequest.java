package org.Core.GameLogic.Api.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;



@Data
public class MoveRequest {

    private String gameId;

    @NotBlank
    @Pattern(regexp = "^[a-hA-H][1-8]$", message = "Invalid square format")
    private String from;

    @NotBlank
    @Pattern(regexp = "^[a-hA-H][1-8]$", message = "Invalid square format")
    private String to;
}
