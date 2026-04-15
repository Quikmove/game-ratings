package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.AdjustmentType;
import lombok.Builder;

@Builder
public record AdjustmentDto(
        String id,
        AdjustmentType type,
        Float value,
        Scale scale
) {
    public record Scale(
            Float min,
            Float max
    ) {}
}
