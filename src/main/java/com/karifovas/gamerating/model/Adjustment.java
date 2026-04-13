package com.karifovas.gamerating.model;

import lombok.*;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@Setter
@Builder
public class Adjustment {
    private final String id;
    private final String name;
    private final String description;
    private final AdjustmentType type;
    private Float value;
    private final ValueScale valueScale;

    public Adjustment copy() {
        return Adjustment.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .value(this.value)
                .valueScale(this.valueScale)
                .build();
    }

    public record ValueScale(
            float min,
            float max
    ) {
    }
}
