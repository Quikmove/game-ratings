package com.karifovas.gamerating.dto;

import lombok.Builder;

@Builder
public record FactorDto(
        String id,
        String name,
        String description,
        Float value
) { }
