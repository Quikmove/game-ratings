package com.karifovas.gamerating.dto;

import jakarta.validation.constraints.NotBlank;

public record AdjustmentInput(
        @NotBlank String gameId,
        @NotBlank String ratingId,
        @NotBlank String adjustmentId,
        Float value
) {
}
