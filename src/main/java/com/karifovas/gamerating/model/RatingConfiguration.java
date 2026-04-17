package com.karifovas.gamerating.model;

import java.math.BigDecimal;
import java.util.List;

public record RatingConfiguration(
        Scale factorScale,
        List<Rating> ratings
) {
    public record Scale(
            BigDecimal min,
            BigDecimal max
    ) {
    }
}
