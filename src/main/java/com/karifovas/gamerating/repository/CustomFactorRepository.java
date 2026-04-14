package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.model.Factor;
import reactor.core.publisher.Mono;

public interface CustomFactorRepository {
    Mono<Factor> findById(String id, String ratingId, String gameId);
    Mono<Boolean> updateValue(String id, String ratingId, String gameId, Float value);
}
