package com.karifovas.gamerating.model;

import java.util.List;

public record Rating(
        String id,
        String gameId,
        String title,
        String description,
        List<Factor> factors,
        Float value,
        RatingStatus status,
        RatingType type,
        List<DrivingRating> drivingRatings,
        List<Adjustment> adjustments
) {
    public record DrivingRating(
            String id,
            float weight
    ) {
    }
}
