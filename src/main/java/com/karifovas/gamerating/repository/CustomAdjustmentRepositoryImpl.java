package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.model.Adjustment;
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
public class CustomAdjustmentRepositoryImpl implements CustomAdjustmentRepository, AdjustmentRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Adjustment> findById(String id, String ratingId, String gameId) {
        var query = Query.query(
                Criteria.where("gameId").is(gameId)
                        .and("id").is(ratingId)
                        .and("adjustments.id").is(id)
        );
        query.fields().elemMatch("adjustments", Criteria.where("id").is(id));

        return mongoTemplate.findOne(query, Rating.class)
                .flatMap(rating -> {
                    if(rating.getAdjustments() == null) {
                        return Mono.empty();
                    }

                    return rating.getAdjustments().stream()
                              .filter(adjustment -> id.equals(adjustment.getId()))
                              .findFirst()
                              .map(Mono::just)
                              .orElseGet(Mono::empty);
                });
    }

    @Override
    public Mono<Boolean> updateValue(String id, String ratingId, String gameId, Float value) {
        var query = Query.query(
                Criteria.where("gameId").is(gameId)
                        .and("id").is(ratingId)
                        .and("adjustments.id").is(id)
        );

        Update update = new Update().set("adjustments.$.value", value);

        return mongoTemplate.updateFirst(query, update, Rating.class)
                .flatMap(result -> result.getMatchedCount() == 0
                        ? Mono.just(false)
                        : Mono.just(true));
    }
}
