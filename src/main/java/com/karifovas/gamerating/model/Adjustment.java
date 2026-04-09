package com.karifovas.gamerating.model;

public record Adjustment(
        String id,
        String name,
        String description,
        AdjustmentType type,
        Float value,
        ValueScale valueScale
) {
    public record ValueScale(
            float min,
            float max
    ) {
    }
}
