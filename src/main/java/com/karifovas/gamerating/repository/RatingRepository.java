package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.model.Rating;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface RatingRepository
extends ReactiveMongoRepository<Rating, String> {
    Mono<Rating> findByIdAndGameId(String id, String gameId);

    Flux<Rating> findAllByGameId(String gameId);
}
