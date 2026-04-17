package com.karifovas.gamerating.service;

import com.karifovas.gamerating.dto.AdjustmentInput;
import com.karifovas.gamerating.exception.EntityNotFoundException;
import com.karifovas.gamerating.exception.ValueOutOfRangeException;
import com.karifovas.gamerating.model.Adjustment;
import com.karifovas.gamerating.model.AdjustmentType;
import com.karifovas.gamerating.repository.AdjustmentRepository;
import com.karifovas.gamerating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AdjustmentService {

    private final AdjustmentRepository adjustmentRepository;
    private final RatingRepository ratingRepository;
    private final RatingCalculationService ratingCalculationService;


    public Mono<Boolean> updateAdjustment(AdjustmentInput input) {
        return adjustmentRepository.findById(input.adjustmentId(), input.ratingId(), input.gameId())
                .switchIfEmpty(
                        Mono.error(
                                new EntityNotFoundException(
                                        Adjustment.class.getSimpleName(),
                                        "Adjustment not found with id: %s, ratingId: %s, gameId: %s"
                                                .formatted(input.adjustmentId(), input.ratingId(), input.gameId())
                                )
                        )
                )
                .flatMap(adjustment -> {
                    var scale = adjustment.getValueScale();

                    if (input.value() != null && (input.value().compareTo(scale.min()) < 0 || input.value().compareTo(scale.max()) > 0)) {
                        return Mono.error(
                                new ValueOutOfRangeException("Invalid value: %s. Expected range: [%s,%s]"
                                        .formatted(input.value(), scale.min(), scale.max())));
                    }

                    if (Objects.equals(adjustment.getValue(), input.value())) {
                        return Mono.just(false);
                    }

                    return adjustmentRepository
                            .updateValue(input.adjustmentId(), input.ratingId(), input.gameId(), input.value())
                            .flatMap(success -> {
                                if (!success) {
                                    return Mono.just(false);
                                }
                                if(adjustment.getType() == AdjustmentType.READ_ONLY) {
                                    return Mono.just(true);
                                }
                                return ratingRepository.findByIdAndGameId(input.ratingId(), input.gameId())
                                        .flatMap(ratingCalculationService::calculateScore);
                            });
                });
    }
}

