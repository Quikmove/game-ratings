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
) {

    public static RatingDto from(Rating rating) {
        return RatingDto.builder()
                .id(rating.getId())
                .code(rating.getCode())
                .name(rating.getName())
                .description(rating.getDescription())
                .factors(rating.getFactors().stream().map(FactorDto::from).toList())
                .adjustments(rating.getAdjustments().stream().map(AdjustmentDto::from).toList())
                .value(rating.getValue())
                .build();
    }
}
