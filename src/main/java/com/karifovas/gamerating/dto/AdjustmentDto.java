package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.AdjustmentType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record AdjustmentDto(
        String id,
        AdjustmentType type,
        BigDecimal value,
        Scale valueScale
) {
    public record Scale(
            BigDecimal min,
            BigDecimal max
    ) {}
}
