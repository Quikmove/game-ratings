package com.karifovas.gamerating.controller;

import com.karifovas.gamerating.dto.AdjustmentInput;
import com.karifovas.gamerating.service.AdjustmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class AdjustmentController {
    private final AdjustmentService adjustmentService;

    @MutationMapping
    public Mono<Boolean> updateAdjustment(@Valid @Argument AdjustmentInput input) {
        return adjustmentService.updateAdjustment(input);
    }
}
