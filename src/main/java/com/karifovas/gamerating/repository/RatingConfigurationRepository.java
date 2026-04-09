package com.karifovas.gamerating.repository;

import com.karifovas.gamerating.model.RatingConfiguration;

import java.io.IOException;

public interface RatingConfigurationRepository {
    RatingConfiguration getRatingConfiguration() throws IOException;
}
