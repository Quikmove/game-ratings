package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.FactorInput;
import com.karifovas.gamerating.exception.ValueOutOfRangeException;
import com.karifovas.gamerating.repository.FactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FactorService {
    private final static float EPSILON = 1e-6f;

    private final FactorRepository factorRepository;
    private final RatingCalculationService calculationService;
    private final RatingService ratingService;
    private final ScaleService scaleService;

    public Mono<Boolean> updateFactor(FactorInput input) {
        return scaleService.getGameFactorScale(input.gameId()).flatMap(
                scale -> {
                    if (input.value() != null && (input.value() < scale.min() || input.value() > scale.max())) {
                        return Mono.error(
                                new ValueOutOfRangeException("Invalid value: %s. Expected range: [%s,%s]"
                                        .formatted(input.value(), scale.min(), scale.max())));
                    }

                    return factorRepository.findById(input.factorId(), input.ratingId(), input.gameId())
                            .flatMap(factor -> {
                                if (!hasChanged(factor.getValue(), input.value())) {
                                    return Mono.just(false);
                                }

                                return factorRepository
                                        .updateValue(input.factorId(), input.ratingId(), input.gameId(), input.value())
                                        .flatMap(successful -> {
                                            if (!successful) {
                                                return Mono.just(false);
                                            }


                                            return ratingService.getRatingByIdAndGameId(input.ratingId(), input.gameId())
                                                    .flatMap(calculationService::calculateScore)
                                                    .thenReturn(true);
                                        });
                            });
                }
        );
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
