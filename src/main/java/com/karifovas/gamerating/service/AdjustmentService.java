package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.AdjustmentInput;
import com.karifovas.gamerating.exception.ValueOutOfRangeException;
import com.karifovas.gamerating.repository.AdjustmentRepository;
import com.karifovas.gamerating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AdjustmentService {

    private static final float EPSILON = 1e-6f;
    private final AdjustmentRepository adjustmentRepository;
    private final RatingRepository ratingRepository;
    private final RatingCalculationService ratingCalculationService;


    public Mono<Boolean> updateAdjustment(AdjustmentInput input) {
        return adjustmentRepository.findById(input.adjustmentId(), input.ratingId(), input.gameId())
                .flatMap(adjustment -> {
                    var scale = adjustment.getValueScale();

                    if (input.value() < scale.min() || input.value() > scale.max()) {
                        return Mono.error(
                                new ValueOutOfRangeException("Invalid value: %s. Expected range: [%s,%s]"
                                        .formatted(input.value(), scale.min(), scale.max())));
                    }

                    if (!hasChanged(adjustment.getValue(), input.value())) {
                        return Mono.just(false);
                    }

                    return adjustmentRepository
                            .updateValue(input.adjustmentId(), input.ratingId(), input.gameId(), input.value())
                            .flatMap(success -> {
                                if (!success) {
                                    return Mono.just(false);
                                }

                                return ratingRepository.findByIdAndGameId(input.ratingId(), input.gameId())
                                        .flatMap(ratingCalculationService::calculateScore);
                            })
                            .thenReturn(true);
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

