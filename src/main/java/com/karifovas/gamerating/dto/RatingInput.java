package com.karifovas.gamerating.dto;

import jakarta.validation.constraints.NotBlank;

public record RatingInput(
        @NotBlank String gameId,
        @NotBlank String ratingId,
        Float value
) {
}
