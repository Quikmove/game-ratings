package com.karifovas.gamerating.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Rating {
    private final String id;
    private final String code;
    private final String gameId;
    private final String name;
    private final String description;
    private List<Factor> factors;
    private Float value;
    private RatingStatus status;
    private final RatingType type;
    private List<DrivingRating> drivingRatings;
    private List<Adjustment> adjustments;

    public Rating deepCopyForGame(String gameId) {
        return Rating.builder()
                .code(this.code)
                .gameId(gameId)
                .name(this.name)
                .description(this.description)
                .factors(this.factors.stream().map(Factor::copy).toList())
                .value(this.value)
                .status(this.status)
                .type(this.type)
                .drivingRatings(this.drivingRatings.stream().map(DrivingRating::copy).toList())
                .adjustments(this.adjustments.stream().map(Adjustment::copy).toList())
                .build();
    }

        public record DrivingRating(String id, String ratingId, float weight) {
            public DrivingRating copy() {
                return new DrivingRating(this.id, this.ratingId, this.weight);
            }
        }
}
