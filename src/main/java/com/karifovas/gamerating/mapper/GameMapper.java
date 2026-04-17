package com.karifovas.gamerating.mapper;

import com.karifovas.gamerating.dto.GameDto;
import com.karifovas.gamerating.model.Game;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface GameMapper {
    GameDto gameToDto(Game game);
}
