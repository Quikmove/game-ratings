package com.karifovas.gamerating.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record RatingInput(
        @NotBlank String gameId,
        @NotBlank String ratingId,
        @Nullable Float value
) {
}
