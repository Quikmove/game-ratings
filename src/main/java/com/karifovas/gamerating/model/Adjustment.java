package com.karifovas.gamerating.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.PersistenceCreator;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class Adjustment {
    private final String id;
    private final String name;
    private final String description;
    private final AdjustmentType type;
    private final ValueScale valueScale;
    private Float value;

    @PersistenceCreator
    public Adjustment(String id, String name, String description, AdjustmentType type, ValueScale valueScale) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.valueScale = valueScale;
    }

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
