package com.karifovas.gamerating.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class Game {
    private String id;
    private String title;
    private String description;
    private final Scale factorScale;

    public record Scale(
            Float min,
            Float max
    ) { }

}
