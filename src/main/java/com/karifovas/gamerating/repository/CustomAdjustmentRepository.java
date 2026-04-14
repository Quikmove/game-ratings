package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.model.Adjustment;
import reactor.core.publisher.Mono;

public interface CustomAdjustmentRepository {
    Mono<Adjustment> findById(String id, String ratingId, String gameId);
    Mono<Boolean> updateValue(String id, String ratingId, String gameId, Float value);
    }
