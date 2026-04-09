package com.karifovas.gamerating.model;

import java.util.List;

public record RatingConfiguration(
        Scale factorScale,
        List<Rating> ratings
) {
    public record Scale(
            float min,
            float max
    ) {
    }
}
