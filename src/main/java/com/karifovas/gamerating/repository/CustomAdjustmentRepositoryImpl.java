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

import java.math.BigDecimal;

@Repository
@RequiredArgsConstructor
public class CustomAdjustmentRepositoryImpl implements CustomAdjustmentRepository, AdjustmentRepository {

    public static final String ADJUSTMENTS_ID_KEY = Rating.Fields.adjustments + "." + Adjustment.Fields.id;
    public static final String ADJUSTMENTS_UPDATE_VALUE_KEY = Rating.Fields.adjustments + ".$." + Adjustment.Fields.value;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<Adjustment> findById(String id, String ratingId, String gameId) {
        var query = Query.query(
                Criteria.where(Rating.Fields.gameId).is(gameId)
                        .and(Rating.Fields.id).is(ratingId)
                        .and(ADJUSTMENTS_ID_KEY).is(id)
        );
        query.fields().elemMatch(Rating.Fields.adjustments, Criteria.where(Adjustment.Fields.id).is(id));

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
    public Mono<Boolean> updateValue(String id, String ratingId, String gameId, BigDecimal value) {
        var query = Query.query(
                Criteria.where(Rating.Fields.gameId).is(gameId)
                        .and(Rating.Fields.id).is(ratingId)
                        .and(ADJUSTMENTS_ID_KEY).is(id)
        );

        Update update = new Update().set(ADJUSTMENTS_UPDATE_VALUE_KEY, value);

        return mongoTemplate.updateFirst(query, update, Rating.class)
                .map(result -> result.getModifiedCount() > 0);
    }
}
