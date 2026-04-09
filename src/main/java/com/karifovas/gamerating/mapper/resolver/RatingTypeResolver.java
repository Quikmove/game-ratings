package com.karifovas.gamerating.mapper.resolver;

import com.karifovas.gamerating.dto.ExternalRatingConfiguration;
import com.karifovas.gamerating.model.RatingType;

public class RatingTypeResolver {

    public static RatingType resolve(ExternalRatingConfiguration.Rating rating) {
        if ("manual".equalsIgnoreCase(rating.type())) {
            return RatingType.MANUAL;
        }

        if (!"calculated".equalsIgnoreCase(rating.type())) {
            throw new IllegalArgumentException(
                    "Unsupported external rating type '%s'.".formatted(rating.type())
            );
        }

        var calculation = rating.calculation();

        if (calculation == null || calculation.method() == null) {
            throw new IllegalArgumentException("Rating type 'calculated' requires a calculation.method value.");
        }

        if ("rating-average".equalsIgnoreCase(calculation.method())) {
            if (calculation.sourceRatings() == null || calculation.sourceRatings().isEmpty()) {
                throw new IllegalArgumentException(
                        "Calculation method 'rating-average' requires at least one source rating."
                );
            }
            return RatingType.RATING_FORMULA;
        }

        if ("factor_average".equalsIgnoreCase(calculation.method())) {
            return RatingType.WEIGHTED_AVERAGE;
        }

        throw new IllegalArgumentException(
                "Unsupported calculation method '%s'."
                        .formatted(calculation.method())
        );
    }
}
