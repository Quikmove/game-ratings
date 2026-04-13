package com.karifovas.gamerating.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Factor {
    private final String id;
    private final String name;
    private final String description;
    private Float value;

    public Factor copy() {
        return Factor.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .value(this.value)
                .build();
    }
}
