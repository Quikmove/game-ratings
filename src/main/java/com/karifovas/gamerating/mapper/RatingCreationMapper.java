package com.karifovas.gamerating.mapper;

import com.karifovas.gamerating.model.Adjustment;
import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.model.Rating;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring"
)
public interface RatingCreationMapper {

    @Mapping(target = "id", source = ".", qualifiedByName = "newId")
    Rating toNewRating(Rating rating);

    @Mapping(target = "id", source = ".", qualifiedByName = "newId")
    Factor toNewFactor(Factor factor);

    @Mapping(target = "id", source = ".", qualifiedByName = "newId")
    Adjustment toNewAdjustment(Adjustment adjustment);

    @Mapping(target = "id", source = ".", qualifiedByName = "newId")
    Rating.DrivingRating toNewDrivingRating(Rating.DrivingRating drivingRating);

    @Named("newId")
    default <T> String newObjectId(T value) {
        return new ObjectId().toString();
    }
}
