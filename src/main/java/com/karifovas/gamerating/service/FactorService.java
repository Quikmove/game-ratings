package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.FactorInput;
import com.karifovas.gamerating.exception.EntityNotFoundException;
import com.karifovas.gamerating.exception.ValueOutOfRangeException;
import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.repository.FactorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class FactorService {

    private final FactorRepository factorRepository;
    private final RatingCalculationService calculationService;
    private final RatingService ratingService;
    private final ScaleService scaleService;

    public Mono<Boolean> updateFactor(FactorInput input) {
        return scaleService.getGameFactorScale(input.gameId()).flatMap(
                scale -> {
                    if (input.value() != null && (input.value().compareTo(scale.min()) < 0 || input.value().compareTo(scale.max()) > 0)) {
                        return Mono.error(
                                new ValueOutOfRangeException("Invalid value: %s. Expected range: [%s,%s]"
                                        .formatted(input.value(), scale.min(), scale.max())));
                    }

                    return factorRepository.findById(input.factorId(), input.ratingId(), input.gameId())
                            .switchIfEmpty(Mono.error(
                                    new EntityNotFoundException(
                                            Factor.class.getSimpleName(),
                                            "Couldn't find factor with id: %s, ratingId: %s, gameId: %s"
                                            .formatted(input.factorId(), input.ratingId(), input.gameId()))
                            ))
                            .flatMap(factor -> {
                                if (Objects.equals(factor.getValue(), input.value())) {
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
}
