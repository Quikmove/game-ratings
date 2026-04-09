package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.dto.ExternalRatingConfiguration;
import com.karifovas.gamerating.mapper.RatingConfigurationMapper;
import com.karifovas.gamerating.model.RatingConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Repository
@RequiredArgsConstructor
public class RatingConfigurationRepositoryImpl implements RatingConfigurationRepository {

    private final RatingConfigurationMapper configMapper;

    @Override
    public RatingConfiguration getRatingConfiguration() throws IOException {
        var objectMapper = new ObjectMapper();

        ExternalRatingConfiguration externalConfiguration = objectMapper.readValue(
                new ClassPathResource("rating_config.json").getInputStream(),
                ExternalRatingConfiguration.class
        );

        return configMapper.map(externalConfiguration);
    }
}
