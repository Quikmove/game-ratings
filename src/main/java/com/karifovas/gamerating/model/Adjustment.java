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

    public record ValueScale(
            BigDecimal min,
            BigDecimal max
    ) {
    }
}
