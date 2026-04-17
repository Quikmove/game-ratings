package com.karifovas.gamerating.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGameInput(
        @NotBlank String name,
        @NotBlank String description
) { }
