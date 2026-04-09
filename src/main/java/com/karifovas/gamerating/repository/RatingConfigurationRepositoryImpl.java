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
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RatingConfigurationRepositoryImpl implements RatingConfigurationRepository {

    private final RatingConfigurationMapper configMapper;
    private final Validator validator;

    @Override
    public RatingConfiguration getRatingConfiguration() throws IOException {
        var objectMapper = new ObjectMapper();

        var externalConfiguration = objectMapper.readValue(
                new ClassPathResource("rating_config.json").getInputStream(),
                ExternalRatingConfiguration.class
        );

        Set<ConstraintViolation<ExternalRatingConfiguration>> violations = validator.validate(externalConfiguration);

        if(!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        return configMapper.map(externalConfiguration);
    }
}
