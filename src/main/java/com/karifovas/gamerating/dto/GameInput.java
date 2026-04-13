package com.karifovas.gamerating.dto;

import jakarta.validation.constraints.NotBlank;

public record GameInput(
        @NotBlank String id,
        String name,
        String description
) {
}
