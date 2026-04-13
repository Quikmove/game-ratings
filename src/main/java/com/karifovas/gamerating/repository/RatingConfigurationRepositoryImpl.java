package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.dto.ExternalRatingConfiguration;
import com.karifovas.gamerating.mapper.RatingConfigurationMapper;
import com.karifovas.gamerating.model.RatingConfiguration;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RatingConfigurationRepositoryImpl implements RatingConfigurationRepository {

    private final RatingConfigurationMapper configMapper;
    private final Validator validator;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<RatingConfiguration> getRatingConfiguration() {
        return Mono.just(getConfigFromFile());
    }

    private RatingConfiguration getConfigFromFile() {
        try {
            ExternalRatingConfiguration externalConfiguration;

            try (var inputStream = new ClassPathResource("rating_config.json").getInputStream()) {
                externalConfiguration = objectMapper.readValue(inputStream, ExternalRatingConfiguration.class);
            }

            Set<ConstraintViolation<ExternalRatingConfiguration>> violations = validator.validate(externalConfiguration);
            if (!violations.isEmpty()) {
                throw new ConstraintViolationException(violations);
            }

            return configMapper.map(externalConfiguration);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load rating configuration", ex);
        }
    }
}
