package com.karifovas.gamerating.dto;

import com.karifovas.gamerating.model.Game;
import lombok.Builder;

@Builder
public record GameDto(
        String id,
        String name,
        String description,
        Scale factorScale

) {
    public record Scale(
            Float min,
            Float max
    ) {
        public static Scale from(Game.Scale gameScale) {
            return new Scale(gameScale.min(), gameScale.max());
        }
    }

    public static GameDto from(Game game) {
        return GameDto.builder()
                .id(game.getId())
                .name(game.getTitle())
                .description(game.getDescription())
                .factorScale(Scale.from(game.getFactorScale()))
                .build();
    }
}
