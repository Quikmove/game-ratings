package com.karifovas.gamerating.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FactorDto(
        String id,
        String name,
        String description,
        BigDecimal value
) { }
