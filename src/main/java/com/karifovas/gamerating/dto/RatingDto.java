package com.karifovas.gamerating.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record RatingDto(
        String id,
        String code,
        String name,
        String description,
        List<FactorDto> factors,
        List<AdjustmentDto> adjustments,
        BigDecimal value
) { }
