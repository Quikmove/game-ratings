package com.karifovas.gamerating.service;

import com.karifovas.gamerating.exception.EntityNotFoundException;
import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.model.Game;
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
                    Map<Rating, Set<Rating>> ratingWithDependencies = allRatings
                            .stream()
                            .collect(Collectors.toMap(
                                    Function.identity(),
                                    r -> r.getDrivingRatings() == null ? Set.of() : r
                                                                                     .getDrivingRatings()
                                                                                     .stream()
                                                                                     .map(dr -> allRatings
                                                                                                .stream()
                                                                                                .filter(candidate -> candidate
                                                                                                                     .getCode()
                                                                                                                     .equals(dr.ratingCode()))
                                                                                                .findFirst()
                                                                                                .orElseThrow())
                                                                                     .collect(Collectors.toSet())
                            ));

                    var affectedRatings = TopologicalSortUtil.findDependents(
                            rating,
                            ratingWithDependencies.keySet(),
                            Rating::getCode,
                            candidate -> allRatings.stream()
                                                   .filter(other -> other.getDrivingRatings() != null && other
                                                           .getDrivingRatings()
                                                           .stream()
                                                           .anyMatch(drivingRating -> candidate.getCode().equals(drivingRating.ratingCode())))
                                                   .toList());

                    var ratingsByCode = allRatings.stream().collect(Collectors.toMap(Rating::getCode, Function.identity()));

                    return Flux.fromIterable(TopologicalSortUtil.sort(
                            affectedRatings,
                            Rating::getCode,
                            candidate -> candidate.getDrivingRatings() == null ? List.of() : candidate
                                                                                             .getDrivingRatings()
                                                                                             .stream()
                                                                                             .map(Rating.DrivingRating::ratingCode)
                                                                                             .toList()))
                               .concatMap(r -> calculateValueByRatingType(r, ratingsByCode)
                                       .doOnNext(r::setValue)
                                       .thenReturn(r));

                })
                .concatMap(ratingRepository::save)
                .then(Mono.just(true));
    }


    private Mono<BigDecimal> calculateValueByRatingType(Rating rating, Map<String, Rating> ratingsByCode) {
        return switch (rating.getType()) {
            case MANUAL -> rating.getValue() == null ? Mono.empty() : Mono.just(rating.getValue());
            case WEIGHTED_AVERAGE -> calculateByFactorAverage(rating.getFactors());
            case RATING_FORMULA -> calculateByRatingFormula(rating.getDrivingRatings(), ratingsByCode);
        };

    }

    private Mono<BigDecimal> calculateByFactorAverage(List<Factor> factors) {
        if (factors
                .stream()
                .anyMatch(f -> f.getValue() == null)) {
            return Mono.empty();
        }

        var sum = factors
                .stream()
                .map(Factor::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var average = sum.divide(
                BigDecimal.valueOf(factors.size()),
                2,
                RoundingMode.HALF_UP
        );

        return Mono.just(average);
    }

    private Mono<BigDecimal> calculateByRatingFormula(List<Rating.DrivingRating> drivingRatings,
                                                      Map<String, Rating> ratingsByCode) {
        var ratingCodes = drivingRatings
                .stream()
                .map(Rating.DrivingRating::ratingCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        if (ratingCodes
                .stream()
                .map(ratingsByCode::get)
                .anyMatch(r -> r.getValue() == null)) {
            return Mono.empty();
        }

        var weightedSum = drivingRatings
                .stream()
                .map(dr -> ratingsByCode
                        .get(dr.ratingCode())
                        .getValue()
                        .multiply(dr.weight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Mono.just(weightedSum);
    }
}
