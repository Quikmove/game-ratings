package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.RatingInput;
import com.karifovas.gamerating.exception.RatingNotFoundException;
import com.karifovas.gamerating.exception.ValueOutOfRangeException;
import com.karifovas.gamerating.model.Rating;
import com.karifovas.gamerating.model.RatingType;
import com.karifovas.gamerating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RatingService {
    private static final float EPSILON = 1e-6f;
    private final RatingRepository ratingRepository;
    private final RatingCalculationService calculationService;
    private final ScaleService scaleService;

    public Flux<Rating> getRatingsByGameId(String gameId) {
        return ratingRepository.findAllByGameId(gameId);
    }

    public Mono<Rating> getRatingByIdAndGameId(String id, String gameId) {
        return ratingRepository.findByIdAndGameId(id, gameId);
    }

    public Mono<Boolean> updateRating(RatingInput input) {
        return ratingRepository.findByIdAndGameId(input.ratingId(), input.gameId())
                .switchIfEmpty(Mono.error(
                        new RatingNotFoundException("Rating not found with id: %s and gameId: %s"
                                .formatted(input.ratingId(), input.gameId()))))
                .flatMap(rating -> {
                    if (rating.getType() != RatingType.MANUAL) {
                        return Mono.error(
                                new RuntimeException("Can't change rating value for non manual rating"));
                    }

                    return scaleService.getGameFactorScale(input.gameId())
                            .flatMap(scale -> {
                                if (input.value() != null && (input.value() < scale.min() || input.value() > scale.max())) {
                                    return Mono.error(
                                            new ValueOutOfRangeException("Value out of range: %s. Expected range: [%s,%s]"
                                                    .formatted(input.value(), scale.min(), scale.max())));
                                }

                                if (!hasChanged(rating.getValue(), input.value())) {
                                    return Mono.just(true);
                                }

                                rating.setValue(input.value());

                                return ratingRepository.save(rating).flatMap(
                                                calculationService::calculateScore
                                        )
                                        .then(Mono.just(true));
                            });
                });
    }

    private boolean hasChanged(Float oldV, Float newV) {
        if (oldV == null && newV == null) {
            return false;
        }

        if (oldV == null) {
            return true;
        }

        if (newV == null) {
            return true;
        }

        return Math.abs(newV - oldV) > EPSILON;
    }
}
