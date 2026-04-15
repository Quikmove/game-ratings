package com.karifovas.gamerating.controller;

import com.karifovas.gamerating.dto.CreateGameInput;
import com.karifovas.gamerating.dto.GameDto;
import com.karifovas.gamerating.dto.GameInput;
import com.karifovas.gamerating.mapper.GameMapper;
import com.karifovas.gamerating.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;
    private final GameMapper gameMapper;

    @QueryMapping
    public Mono<List<GameDto>> games() {
        return gameService.findAll().map(gameMapper::gameToDto).collectList();
    }

    @QueryMapping
    public Mono<GameDto> game(@Argument String id) {
        return gameService.findById(id).map(gameMapper::gameToDto);
    }

    @MutationMapping
    public Mono<GameDto> createGame(@Valid @Argument CreateGameInput input) {
        return gameService.createGame(input).map(gameMapper::gameToDto);
    }

    @MutationMapping
    public Mono<Boolean> updateGame(@Valid @Argument GameInput input) {
        return gameService.updateGame(input);
    }
}
