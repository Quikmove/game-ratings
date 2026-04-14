package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.CreateGameInput;
import com.karifovas.gamerating.dto.GameInput;
import com.karifovas.gamerating.exception.GameNotFoundException;
import com.karifovas.gamerating.model.Adjustment;
import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.model.Game;
import com.karifovas.gamerating.model.Rating;
import com.karifovas.gamerating.repository.GameRepository;
import com.karifovas.gamerating.repository.RatingConfigurationRepository;
import com.karifovas.gamerating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
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
                                        .map(this::generateIdsForRating)
                                        .toList();

                                return ratingRepository
                                        .saveAll(ratings)
                                        .then(Mono.just(savedGame));
                            });
                });
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

                    return gameRepository.save(game)
                            .then(Mono.just(true));
                })
                .switchIfEmpty(Mono.error(
                        new GameNotFoundException("Game not found with id: %s".formatted(input.id()))));
    }

    private Rating generateIdsForRating(Rating rating) {
        if (rating == null) {
            return null;
        }

        if (rating.getFactors() != null) {
            rating.setFactors(
                    rating.getFactors().stream()
                            .map(factor -> factor.getId() == null ?
                                    Factor.builder()
                                    .id(new ObjectId().toString())
                                    .code(factor.getCode())
                                    .name(factor.getName())
                                    .description(factor.getDescription())
                                    .value(factor.getValue())
                                    .build()
                                    : factor)
                            .toList()
            );
        }

        if (rating.getAdjustments() != null) {
            rating.setAdjustments(
                    rating.getAdjustments().stream()
                            .map(adjustment -> adjustment.getId() == null ?
                                    Adjustment.builder()
                                    .id(new ObjectId().toString())
                                    .code(adjustment.getCode())
                                    .name(adjustment.getName())
                                    .description(adjustment.getDescription())
                                    .type(adjustment.getType())
                                    .valueScale(adjustment.getValueScale())
                                    .value(adjustment.getValue())
                                    .build()
                                    : adjustment)
                            .toList()
            );
        }

        if (rating.getDrivingRatings() != null) {
            rating.setDrivingRatings(
                    rating.getDrivingRatings().stream()
                            .map(dr -> dr.id() == null ? new Rating.DrivingRating(new ObjectId().toString(), dr.ratingCode(), dr.weight()) : dr)
                            .toList()
            );
        }

        return rating;
    }
}
