package com.karifovas.gamerating.model;

public record Adjustment(
        String type,
        Float value,
        ValueScale valueScale
) {
    public record ValueScale(
            float min,
            float max
    ) {
    }
}
