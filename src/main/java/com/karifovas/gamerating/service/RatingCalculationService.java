package com.karifovas.gamerating.service;

import com.karifovas.gamerating.exception.EntityNotFoundException;
import com.karifovas.gamerating.model.*;
import com.karifovas.gamerating.repository.RatingRepository;
import com.karifovas.gamerating.utils.TopologicalSortUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingCalculationService {
    private final RatingRepository ratingRepository;

    public Mono<Boolean> calculateScore(Rating rating) {
        return ratingRepository
                .findAllByGameId(rating.getGameId())
                .switchIfEmpty(
                        Mono.error(
                                new EntityNotFoundException(
                                        Game.class.getSimpleName(),
                                        "Game not found with id: %s"
                                                .formatted(rating.getGameId())
                                )
                        )
                )
                .collectList()
                .flatMapMany(allRatings -> {
                    var ratingsByCode = allRatings
                            .stream()
                            .collect(Collectors.toMap(Rating::getCode, Function.identity()));

                    var affectedRatings = TopologicalSortUtil.findDependents(
                            rating,
                            allRatings,
                            Rating::getCode,
                            candidate -> allRatings
                                    .stream()
                                    .filter(other -> other.getDrivingRatings() != null && other
                                            .getDrivingRatings()
                                            .stream()
                                            .anyMatch(dr -> candidate
                                                    .getCode()
                                                    .equals(dr.ratingCode())))
                                    .toList());

                    return Flux
                            .fromIterable(
                                    TopologicalSortUtil.sort(
                                            affectedRatings,
                                            Rating::getCode,
                                            r -> r.getDrivingRatings() == null ? List.of() :
                                                    r
                                                    .getDrivingRatings()
                                                    .stream()
                                                    .map(Rating.DrivingRating::ratingCode)
                                                    .toList()
                                    ))
                            .concatMap(r -> {
                                var computedBaseValue = calculateValueByRatingType(r, ratingsByCode);
                                var computedAdjustedValue = applyImpactfulAdjustments(computedBaseValue,
                                                                                      r.getAdjustments());

                                r.setValue(computedAdjustedValue);
                                return Mono.just(r);
                            });
                })
                .concatMap(ratingRepository::save)
                .then(Mono.just(true));
    }

    private BigDecimal calculateValueByRatingType(Rating rating, Map<String, Rating> ratingsByCode) {
        return switch (rating.getType()) {
            case MANUAL -> rating.getValue();
            case WEIGHTED_AVERAGE -> calculateByFactorAverage(rating.getFactors());
            case RATING_FORMULA -> calculateByRatingFormula(rating.getDrivingRatings(), ratingsByCode);
        };
    }

    private BigDecimal calculateByFactorAverage(List<Factor> factors) {
        if (factors == null || factors.isEmpty() || factors
                .stream()
                .anyMatch(f -> f.getValue() == null)) {
            return null;
        }

        return factors
                .stream()
                .map(Factor::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(factors.size()), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateByRatingFormula(List<Rating.DrivingRating> drivingRatings,
                                                Map<String, Rating> ratingsByCode) {
        if (drivingRatings == null || drivingRatings.isEmpty()) {
            return null;
        }

        if (drivingRatings
                .stream()
                .map(dr -> ratingsByCode.get(dr.ratingCode()))
                .anyMatch(r -> r.getValue() == null)) {
            return null;
        }

        return drivingRatings
                .stream()
                .map(Rating.DrivingRating::ratingCode)
                .map(ratingsByCode::get)
                .map(Rating::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal applyImpactfulAdjustments(BigDecimal value, List<Adjustment> adjustments) {
        if (value == null) {
            return null;
        }

        if (adjustments == null || adjustments.isEmpty()) {
            return value;
        }

        return adjustments
                .stream()
                .filter(a -> a.getType() == AdjustmentType.IMPACTFUL)
                .map(Adjustment::getValue)
                .filter(Objects::nonNull)
                .reduce(value, BigDecimal::add);
    }
}
