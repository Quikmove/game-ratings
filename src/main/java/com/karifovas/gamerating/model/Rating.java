package com.karifovas.gamerating.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldNameConstants
@EqualsAndHashCode
public class Rating {
    private final String id;
    private final String code;
    private final String gameId;
    private final String name;
    private final String description;
    private List<Factor> factors;
    private BigDecimal value;
    private RatingStatus status;
    private final RatingType type;
    private List<DrivingRating> drivingRatings;
    private List<Adjustment> adjustments;

    @Builder
        public record DrivingRating(String id, String ratingCode, BigDecimal weight) {
    }
}
