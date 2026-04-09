package com.karifovas.gamerating.dto;

import java.util.List;

public record ExternalRatingConfiguration(
        Configuration configuration,
        Scale scale,
        List<Game> games,
        List<Rating> ratings
) {
    public record Configuration(
            Scale factorScale,
            AdjustmentScale adjustmentScale
    ) {
    }

    public record Scale(
            float min,
            float max
    ) {
    }

    public record AdjustmentScale(
            float min,
            float max
    ) {
    }

    public record Game(
            String id,
            String title,
            String description
    ) {
    }

    public record Rating(
            String id,
            String title,
            String name,
            String description,
            List<Factor> factors,
            String status,
            String type,
            Calculation calculation,
            List<DrivingRating> drivingRatings,
            List<Adjustment> adjustments
    ) {
    }

    public record Factor(
            String id,
            String title,
            String name,
            String description
    ) {
    }

    public record Calculation(
            String method,
            List<SourceRating> sourceRatings
    ) {
    }

    public record SourceRating(
            String ratingId,
            Float weight
    ) {
    }

    public record DrivingRating(
            String id,
            Float weight
    ) {
    }

    public record Adjustment(
            String type,
            ValueScale valueScale,
            String impact,
            Scale valueRange
    ) {
    }

    public record ValueScale(
            float min,
            float max
    ) {
    }
}
