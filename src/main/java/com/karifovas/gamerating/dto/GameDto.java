package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.Game;
import lombok.Builder;

@Builder
public record GameDto(
        String id,
        String name,
        String description,
        Scale factorScale

) {
    public record Scale(
            Float min,
            Float max
    ) { }
}
