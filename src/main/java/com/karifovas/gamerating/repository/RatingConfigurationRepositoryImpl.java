package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.dto.ExternalRatingConfiguration;
import com.karifovas.gamerating.mapper.RatingConfigurationMapper;
import com.karifovas.gamerating.model.RatingConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Repository
public class RatingConfigurationRepositoryImpl implements RatingConfigurationRepository {

    private final RatingConfiguration config;

    public RatingConfigurationRepositoryImpl(RatingConfigurationMapper mapper) throws IOException {
        var objectMapper = new ObjectMapper();

        ExternalRatingConfiguration externalConfiguration = objectMapper.readValue(
                new ClassPathResource("rating_config.json").getInputStream(),
                ExternalRatingConfiguration.class
        );

        this.config = mapper.map(externalConfiguration);
    }

    @Override
    public RatingConfiguration getRatingConfiguration() {
        return config;
    }
}
