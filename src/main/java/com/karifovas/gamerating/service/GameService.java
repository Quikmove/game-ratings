package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.CreateGameInput;
import com.karifovas.gamerating.dto.GameInput;
import com.karifovas.gamerating.exception.GameNotFoundException;
import com.karifovas.gamerating.mapper.RatingCreationMapper;
import com.karifovas.gamerating.model.Game;
import com.karifovas.gamerating.model.Rating;
import com.karifovas.gamerating.repository.GameRepository;
import com.karifovas.gamerating.repository.RatingConfigurationRepository;
import com.karifovas.gamerating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final RatingRepository ratingRepository;
    private final RatingConfigurationRepository configurationRepository;
    private final RatingCreationMapper ratingCreationMapper;

    public Flux<Game> findAll() {
        return gameRepository.findAll();
    }

    public Mono<Game> findById(String id) {
        return gameRepository.findById(id);
    }


    public Mono<Game> createGame(CreateGameInput input) {
        return configurationRepository.getRatingConfiguration()
                .flatMap(configuration -> {
                    var scale = configuration.factorScale();

                    var game = Game.builder()
                            .name(input.name())
                            .description(input.description())
                            .factorScale(new Game.Scale(scale.min(), scale.max()))
                            .build();

                    return gameRepository.save(game)
                            .flatMap(savedGame -> {
                                var ratings = configuration.ratings().stream()
                                        .map(rating -> rating.deepCopyForGame(savedGame.getId()))
                                        .map(rating -> (Rating) rating) // fix for compile error when applying below static method
                                        .map(ratingCreationMapper::toNewRating)
                                        .toList();

                                return ratingRepository
                                        .saveAll(ratings)
                                        .then(Mono.just(savedGame));
                            });
                });
    }

    public Mono<Boolean> updateGame(GameInput input) {
        return gameRepository.findById(input.id())
                .switchIfEmpty(
                        Mono.error(
                                new GameNotFoundException("Game not found with id: %s"
                                        .formatted(input.id())))
                )
                .flatMap(game -> {
                    if (input.name() != null) {
                        game.setName(input.name());
                    }
                    if (input.description() != null) {
                        game.setDescription(input.description());
                    }

                    return gameRepository.save(game)
                            .then(Mono.just(true));
                });
    }
}
