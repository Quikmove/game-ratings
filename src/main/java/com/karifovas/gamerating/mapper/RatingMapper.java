package com.karifovas.gamerating.mapper;

import com.karifovas.gamerating.dto.RatingDto;
import com.karifovas.gamerating.model.Rating;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring"
)
public interface RatingMapper {
    RatingDto ratingToDto(Rating rating);
}
