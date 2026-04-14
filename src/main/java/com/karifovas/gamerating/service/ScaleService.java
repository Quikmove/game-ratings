package com.karifovas.gamerating.service;

import com.karifovas.gamerating.exception.AdjustmentNotFoundException;
import com.karifovas.gamerating.exception.GameNotFoundException;
import com.karifovas.gamerating.model.Adjustment;
import com.karifovas.gamerating.model.Game;
import com.karifovas.gamerating.repository.AdjustmentRepository;
import com.karifovas.gamerating.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ScaleService {

    private final GameRepository gameRepository;
    private final AdjustmentRepository adjustmentRepository;

    public Mono<Game.Scale> getGameFactorScale(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(
                        new GameNotFoundException("Couldn't find game with id %s"
                                .formatted(gameId))
                ))
                .map(Game::getFactorScale);
    }

    public Mono<Adjustment.ValueScale> getAdjustmentValueScale(String adjustmentId, String ratingId, String gameId) {
        return adjustmentRepository.findById(adjustmentId, ratingId, gameId)
                .switchIfEmpty(Mono.error(
                        new AdjustmentNotFoundException("Couldn't find adjustment with id: %s, ratingId: %s, gameId: %s"
                                .formatted(adjustmentId, ratingId, gameId))
                ))
                .map(Adjustment::getValueScale);
    }
}
