package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.Factor;
import lombok.Builder;

@Builder
public record FactorDto(
        String id,
        String name,
        String description,
        Float value
) {
    public static FactorDto from(Factor factor) {
        return FactorDto.builder()
                .id(factor.getId())
                .name(factor.getName())
                .description(factor.getDescription())
                .value(factor.getValue())
                .build();
    }
}
