package com.karifovas.gamerating.model;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Rating {
    private String id;
    private String gameId;
    private String name;
    private String description;
    private List<Factor> factors = List.of();
    private Float value;
    private RatingStatus status;
    private RatingType type;
    private List<DrivingRating> drivingRatings = List.of();
    private List<Adjustment> adjustments = List.of();

    public Rating deepCopyForGame(String gameId) {
        return Rating.builder()
                .id(this.id)
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


    @Getter
    @Setter
    @AllArgsConstructor
    public static class DrivingRating {
        private String id;
        private float weight;

        public DrivingRating copy() {
            return new DrivingRating(this.id, this.weight);
        }
    }
}
