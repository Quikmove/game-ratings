package com.karifovas.gamerating.controller;

import com.karifovas.gamerating.dto.CreateGameInput;
import com.karifovas.gamerating.dto.GameDto;
import com.karifovas.gamerating.dto.GameInput;
import com.karifovas.gamerating.service.GameService;
import jakarta.validation.Valid;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @QueryMapping(name = "games")
    public Mono<List<GameDto>> games() {
        return gameService.findAll().map(GameDto::from).collectList();
    }

    @QueryMapping(name = "game")
    public Mono<GameDto> game(@Argument String id) {
        return gameService.findById(id).map(GameDto::from);
    }

    @MutationMapping(name = "createGame")
    public Mono<GameDto> createGame(@Valid @Argument CreateGameInput input) {
        return gameService.createGame(input).map(GameDto::from);
    }

    @MutationMapping(name = "updateGame")
    public Mono<Boolean> updateGame(@Valid @Argument GameInput input) {
        return gameService.updateGame(input);
    }
}
