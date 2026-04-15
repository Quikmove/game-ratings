package com.karifovas.gamerating.model;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Game {
    private final String id;
    private String name;
    private String description;
    private final Scale factorScale;

    public record Scale(
            BigDecimal min,
            BigDecimal max
    ) { }

}
