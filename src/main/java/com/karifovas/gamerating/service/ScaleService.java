package com.karifovas.gamerating.service;

import com.karifovas.gamerating.exception.GameNotFoundException;
import com.karifovas.gamerating.model.Game;
import com.karifovas.gamerating.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ScaleService {

    private final GameRepository gameRepository;

    public Mono<Game.Scale> getGameFactorScale(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(
                        new GameNotFoundException("Couldn't find game with id %s"
                                .formatted(gameId))
                ))
                .map(Game::getFactorScale);
    }
}
