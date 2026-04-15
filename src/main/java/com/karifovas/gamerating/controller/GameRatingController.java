package com.karifovas.gamerating.controller;

import com.karifovas.gamerating.dto.AdjustmentInput;
import com.karifovas.gamerating.dto.FactorInput;
import com.karifovas.gamerating.dto.RatingDto;
import com.karifovas.gamerating.dto.RatingInput;
import com.karifovas.gamerating.mapper.RatingMapper;
import com.karifovas.gamerating.service.AdjustmentService;
import com.karifovas.gamerating.service.FactorService;
import com.karifovas.gamerating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GameRatingController {
    private final RatingService ratingService;
    private final RatingMapper ratingMapper;

    @QueryMapping(name = "gameRatings")
    public Mono<List<RatingDto>> gameRatings(@Argument String gameId) {
        return ratingService.getRatingsByGameId(gameId).map(ratingMapper::ratingToDto).collectList();
    }

    @QueryMapping(name = "gameRating")
    public Mono<RatingDto> gameRating(@Argument String gameId,@Argument String id) {
        return ratingService.getRatingByIdAndGameId(id, gameId).map(ratingMapper::ratingToDto);
    }

    @MutationMapping(name = "updateRating")
    public Mono<Boolean> updateRating(@Valid @Argument RatingInput input) {
        return ratingService.updateRating(input);
    }
}
