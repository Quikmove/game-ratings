package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.Rating;
import lombok.Builder;

import java.util.List;

@Builder
public record RatingDto(
        String id,
        String code,
        String name,
        String description,
        List<FactorDto> factors,
        List<AdjustmentDto> adjustments,
        Float value
) { }
