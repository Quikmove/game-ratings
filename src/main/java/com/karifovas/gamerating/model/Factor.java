package com.karifovas.gamerating.model;

import lombok.*;
import org.springframework.data.annotation.PersistenceCreator;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Factor {
    private final String id;
    private final String name;
    private final String description;
    private Float value;

    @PersistenceCreator
    public Factor(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Factor copy() {
        return Factor.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .value(this.value)
                .build();
    }
}
