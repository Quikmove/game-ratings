package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.model.Rating;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class CustomFactorRepositoryImpl implements FactorRepository, CustomFactorRepository {

    private static final String FACTOR_ID_KEY = Rating.Fields.factors + "." + Factor.Fields.id;
    private static final String FACTOR_UPDATE_VALUE_KEY = Rating.Fields.factors + ".$." + Factor.Fields.value;

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Factor> findById(String id, String ratingId, String gameId) {
        var query = new Query(
                Criteria.where(Rating.Fields.id).is(ratingId)
                        .and(Rating.Fields.gameId).is(gameId)
                        .and(FACTOR_ID_KEY).is(id)
        );

        return mongoTemplate.findOne(query, Rating.class)
                .flatMap(rating -> {
                    if (rating.getFactors() == null) {
                        return Mono.empty();
                    }
                    return rating.getFactors().stream()
                            .filter(factor -> id.equals(factor.getId())
                            ).findFirst()
                            .map(Mono::just)
                            .orElseGet(Mono::empty);
                });
    }

    @Override
    public Mono<Boolean> updateValue(String id, String ratingId, String gameId, Float value) {
        var query = new Query(
                Criteria.where(Rating.Fields.id).is(ratingId)
                        .and(Rating.Fields.gameId).is(gameId)
                        .and(FACTOR_ID_KEY).is(id)
        );

        var update = new Update().set(FACTOR_UPDATE_VALUE_KEY, value);

        return mongoTemplate.updateFirst(query, update, Rating.class)
                .map(result -> result.getModifiedCount() > 0);
    }
}
