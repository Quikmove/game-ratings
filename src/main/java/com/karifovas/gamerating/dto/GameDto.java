package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.Game;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record GameDto(
        String id,
        String name,
        String description,
        Scale factorScale

) {
    public record Scale(
            BigDecimal min,
            BigDecimal max
    ) { }
}
