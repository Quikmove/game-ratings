package com.karifovas.gamerating.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum AdjustmentType {
    READ_ONLY("read_only"),
    IMPACTFUL("affects_calculation");

    private final String configurationReference;

    public static AdjustmentType resolveFromExternalConfiguration(String value) {
        return Arrays.stream(AdjustmentType.values())
                .filter(type -> type
                        .getConfigurationReference().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid adjustment type '%s'."
                                .formatted(value)
                ));
    }
}
