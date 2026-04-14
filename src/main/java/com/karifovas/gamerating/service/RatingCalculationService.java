package com.karifovas.gamerating.service;

import com.karifovas.gamerating.exception.MissingValueException;
import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.model.Rating;
import com.karifovas.gamerating.repository.RatingRepository;
import com.karifovas.gamerating.utils.TopologicalSortUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class RatingCalculationService {
    private final RatingRepository ratingRepository;

    public Mono<Boolean> calculateScore(Rating rating) {
        return ratingRepository.findAllByGameId(rating.getGameId())
                .collectList()
                .flatMap(allRatings -> {
                    Map<String, Rating> ratingsByCode = Stream.concat(allRatings.stream(), Stream.of(rating))
                            .collect(Collectors.toMap(Rating::getCode, Function.identity(),
                                    (left, right) -> right, HashMap::new));

                    Set<String> affectedCodes = TopologicalSortUtil.findDependents(
                            rating.getCode(),
                            ratingsByCode.values(),
                            Rating::getCode,
                            candidate -> candidate.getDrivingRatings().stream()
                                    .map(Rating.DrivingRating::ratingCode)
                                    .toList());

                    List<Rating> orderedRatings = TopologicalSortUtil.sort(
                            affectedCodes.stream()
                                    .map(ratingsByCode::get)
                                    .filter(Objects::nonNull)
                                    .toList(),
                            Rating::getCode,
                            candidate -> candidate.getDrivingRatings().stream()
                                    .map(Rating.DrivingRating::ratingCode)
                                    .toList());

                    return Flux.fromIterable(orderedRatings)
                            .concatMap(currentRating ->
                                    calculateValue(currentRating, ratingsByCode)
                                            .flatMap(value -> {
                                                currentRating.setValue(value);
                                                return ratingRepository.save(currentRating);
                                            })
                            )
                            .then(Mono.just(true));
                });
    }


    private Mono<Float> calculateValue(Rating rating, Map<String, Rating> ratingsByCode) {
        if (rating.getType() == null) {
            return Mono.error(new MissingValueException("Rating type is missing"));
        }

        return switch (rating.getType()) {
            case MANUAL -> {
                if (rating.getValue() == null) {
                    yield Mono.error(new MissingValueException("Manual rating value is missing"));
                }
                yield Mono.just(rating.getValue());
            }
            case WEIGHTED_AVERAGE -> calculateByFactorAverage(rating.getFactors());
            case RATING_FORMULA -> calculateByRatingFormula(rating.getDrivingRatings(), ratingsByCode);
        };

    }

    private Mono<Float> calculateByFactorAverage(List<Factor> factors) {
        if (factors.stream().anyMatch(f -> f.getValue() == null)) {
            return Mono.error(new MissingValueException("Not all factors have values"));
        }

        return Mono.just((float) factors.stream()
                .map(Factor::getValue)
                .mapToDouble(Float::doubleValue)
                .average()
                .orElseThrow());
    }

    private Mono<Float> calculateByRatingFormula(List<Rating.DrivingRating> drivingRatings, Map<String, Rating> ratingsByCode) {
        Set<String> ratingCodes = drivingRatings.stream()
                .map(Rating.DrivingRating::ratingCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));


        if (ratingCodes.stream().map(ratingsByCode::get).anyMatch(r -> r.getValue() == null)) {
            return Mono.error(new MissingValueException("Some referenced ratings have no value"));
        }

        double weightedSum = drivingRatings.stream()
                .mapToDouble(dr -> ratingsByCode.get(dr.ratingCode()).getValue() * dr.weight())
                .sum();

        return Mono.just((float) weightedSum);
    }
}
