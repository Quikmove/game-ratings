package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.GameInput;
import com.karifovas.gamerating.model.Game;
import com.karifovas.gamerating.repository.GameRepository;
import com.karifovas.gamerating.repository.RatingConfigurationRepository;
import com.karifovas.gamerating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final RatingRepository ratingRepository;
    private final RatingConfigurationRepository configurationRepository;

    public Flux<Game> findAll() {
        return gameRepository.findAll();
    }

    public Mono<Game> findById(String id) {
        return gameRepository.findById(id);
    }


    public Mono<Game> createGame(GameInput input) {
        return configurationRepository.getRatingConfiguration()
                .flatMap(configuration -> {
                    var scale = configuration.factorScale();
                    var ratings = configuration.ratings().stream()
                            .map(rating -> rating.deepCopyForGame(input.id()))
                            .toList();

                    var game = Game.builder()
                            .id(input.id())
                            .name(input.name())
                            .description(input.description())
                            .factorScale(new Game.Scale(scale.min(), scale.max()))
                            .build();

                    return gameRepository.save(game)
                            .flatMap(savedGame -> ratingRepository
                                    .saveAll(ratings)
                                    .then(Mono.just(savedGame)));
                })
                .doOnError(error ->
                        log.error("Failed to create game {}", input.id(), error));
    }

    public Mono<Boolean> updateGame(GameInput input) {
        return gameRepository.findById(input.id())
                .flatMap(game -> {
                    if(input.name() != null) {
                        game.setName(input.name());
                    }
                    if(input.description() != null) {
                        game.setDescription(input.description());
                    }

                    return Mono.just(true);
                })
                .switchIfEmpty(Mono.just(false));
    }

    public Mono<Game.Scale> getScale(String gameId) {
        return gameRepository.findById(gameId).map(Game::getFactorScale);
    }
}
