package com.karifovas.gamerating.controller;

import com.karifovas.gamerating.dto.FactorInput;
import com.karifovas.gamerating.service.FactorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
public class FactorController {
    private final FactorService factorService;
    
    @MutationMapping
    public Mono<Boolean> updateFactor(@Valid @Argument FactorInput input) {
        return factorService.updateFactor(input);
    }

}
