package com.karifovas.gamerating.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record FactorInput(
        @NotBlank String gameId,
        @NotBlank String ratingId,
        @NotBlank String factorId,
        @Nullable Float value
) {
}
