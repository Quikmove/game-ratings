package com.karifovas.gamerating.dto;

import jakarta.validation.constraints.NotBlank;

public record FactorInput(
        @NotBlank String gameId,
        @NotBlank String ratingId,
        @NotBlank String factorId,
        Float value
) {
}
