package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.model.RatingConfiguration;
import reactor.core.publisher.Mono;


public interface RatingConfigurationRepository {
    Mono<RatingConfiguration> getRatingConfiguration();
}
