package com.karifovas.gamerating.service;

import com.karifovas.gamerating.exception.EntityNotFoundException;
import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.model.Rating;
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RatingCalculationService {
    private final RatingRepository ratingRepository;

    public Mono<Boolean> calculateScore(Rating rating) {
        return ratingRepository.findAllByGameId(rating.getGameId())
                .switchIfEmpty(
                        Mono.error(
                                new EntityNotFoundException(
                                        Rating.class.getSimpleName(),
                                        "Ratings not found with gameId: %s"
                                                .formatted(rating.getGameId())
                                )
                        )
                )
                .collectList()
                .flatMap(allRatings -> {
                    Map<String, Rating> ratingsByCode = Stream.concat(allRatings.stream(), Stream.of(rating))
                            .collect(Collectors.toMap(Rating::getCode, Function.identity(),
                                    (left, right) -> right));

                    Set<String> affectedCodes = TopologicalSortUtil.findDependents(
                            rating.getCode(),
                            ratingsByCode.values(),
                            Rating::getCode,
                            candidate -> candidate.getDrivingRatings() == null ? List.of() : candidate.getDrivingRatings().stream()
                                    .map(Rating.DrivingRating::ratingCode)
                                    .toList());

                    List<Rating> orderedRatings = TopologicalSortUtil.sort(
                            affectedCodes.stream()
                                    .map(ratingsByCode::get)
                                    .filter(Objects::nonNull)
                                    .toList(),
                            Rating::getCode,
                            candidate -> candidate.getDrivingRatings() == null ? List.of() : candidate.getDrivingRatings().stream()
                                    .map(Rating.DrivingRating::ratingCode)
                                    .toList());

                    return Flux.fromIterable(orderedRatings)
                            .concatMap(currentRating ->
                                    calculateValue(currentRating, ratingsByCode)
                                            .flatMap(value -> {
                                                currentRating.setValue(value);
                                                return ratingRepository.save(currentRating);
                                            })
                                            .switchIfEmpty(Mono.defer(() -> {
                                                currentRating.setValue(null);
                                                return ratingRepository.save(currentRating);
                                            }))

                            )
                            .then(Mono.just(true));
                });
    }


    private Mono<BigDecimal> calculateValue(Rating rating, Map<String, Rating> ratingsByCode) {
        return switch (rating.getType()) {
            case MANUAL -> {
                if (rating.getValue() == null) {
                    yield Mono.empty();
                }
                yield Mono.just(rating.getValue());
            }
            case WEIGHTED_AVERAGE -> calculateByFactorAverage(rating.getFactors());
            case RATING_FORMULA -> calculateByRatingFormula(rating.getDrivingRatings(), ratingsByCode);
        };

    }

    private Mono<BigDecimal> calculateByFactorAverage(List<Factor> factors) {
        if (factors.stream().anyMatch(f -> f.getValue() == null)) {
            return Mono.empty();
        }

        var sum = factors.stream()
                .map(Factor::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var average = sum.divide(
                BigDecimal.valueOf(factors.size()),
                2,
                RoundingMode.HALF_UP
        );

        return Mono.just(average);
    }

    private Mono<BigDecimal> calculateByRatingFormula(List<Rating.DrivingRating> drivingRatings, Map<String, Rating> ratingsByCode) {
        Set<String> ratingCodes = drivingRatings.stream()
                .map(Rating.DrivingRating::ratingCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        if (ratingCodes.stream().map(ratingsByCode::get).anyMatch(r -> r.getValue() == null)) {
            return Mono.empty();
        }

        var weightedSum = drivingRatings.stream()
                .map(dr -> ratingsByCode.get(dr.ratingCode()).getValue().multiply(dr.weight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Mono.just(weightedSum);
    }
}
