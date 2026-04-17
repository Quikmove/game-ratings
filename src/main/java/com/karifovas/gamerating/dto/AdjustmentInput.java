package com.karifovas.gamerating.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AdjustmentInput(
        @NotBlank String gameId,
        @NotBlank String ratingId,
        @NotBlank String adjustmentId,
        @Nullable BigDecimal value
) {
}
