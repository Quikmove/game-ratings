package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.Adjustment;
import com.karifovas.gamerating.model.AdjustmentType;
import lombok.Builder;

@Builder
public record AdjustmentDto(
        String id,
        AdjustmentType type,
        Float value,
        Scale scale
) {
    public static AdjustmentDto from(Adjustment adjustment) {
        return AdjustmentDto.builder()
                .id(adjustment.getId())
                .type(adjustment.getType())
                .value(adjustment.getValue())
                .scale(new Scale(adjustment.getValueScale().min(), adjustment.getValueScale().max()))
                .build();
    }

    public record Scale(
            Float min,
            Float max
    ) {}
}
