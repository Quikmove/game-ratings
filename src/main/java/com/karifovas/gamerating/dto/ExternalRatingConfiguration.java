package com.karifovas.gamerating.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ExternalRatingConfiguration(
        @NotNull @Valid Scale scale,
        @NotNull @Valid List<@Valid Rating> ratings
) {
    public record Scale(
            @NotNull BigDecimal min,
            @NotNull BigDecimal max
    ) {
    }

    public record Rating(
            @NotNull String id,
            @NotNull String name,
            @NotNull String description,
            @NotNull @Valid List<@Valid Factor> factors,
            @NotNull String type,
            @Valid Calculation calculation,
            @NotNull @Valid List<@Valid Adjustment> adjustments
    ) {
    }

    public record Factor(
            @NotNull String id,
            @NotNull String name,
            @NotNull String description
    ) {
    }

    public record Calculation(
            @NotNull String method,
            @Valid List<@Valid SourceRating> sourceRatings
    ) {
    }

    public record SourceRating(
            @NotBlank String ratingId,
            @NotNull BigDecimal weight
    ) {
    }

    public record Adjustment(
            @NotNull String id,
            @NotNull String name,
            @NotNull String description,
            @NotNull String impact,
            @NotNull @Valid Scale valueRange
    ) {
    }
}
