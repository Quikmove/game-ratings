package com.karifovas.gamerating.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@FieldNameConstants
public class Factor {
    private final String id;
    private final String code;
    private final String name;
    private final String description;
    private BigDecimal value;

    public Factor copy() {
        return Factor.builder()
                .id(this.id)
                .code(this.code)
                .name(this.name)
                .description(this.description)
                .value(this.value)
                .build();
    }
}
