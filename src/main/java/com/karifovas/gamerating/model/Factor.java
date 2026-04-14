package com.karifovas.gamerating.model;

import lombok.*;
import lombok.experimental.FieldNameConstants;

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
    private Float value;

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
