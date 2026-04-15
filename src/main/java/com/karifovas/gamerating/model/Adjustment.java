package com.karifovas.gamerating.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldNameConstants
public class Adjustment {
    private final String id;
    private final String code;
    private final String name;
    private final String description;
    private final AdjustmentType type;
    private final ValueScale valueScale;
    private BigDecimal value;

    public Adjustment copy() {
        return Adjustment.builder()
                .id(this.id)
                .code(this.code)
                .name(this.name)
                .description(this.description)
                .type(this.type)
                .value(this.value)
                .valueScale(this.valueScale)
                .build();
    }

    public record ValueScale(
            BigDecimal min,
            BigDecimal max
    ) {
    }
}
