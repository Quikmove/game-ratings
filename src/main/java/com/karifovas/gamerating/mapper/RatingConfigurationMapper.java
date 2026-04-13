package com.karifovas.gamerating.mapper;

import com.karifovas.gamerating.dto.ExternalRatingConfiguration;
import com.karifovas.gamerating.mapper.resolver.RatingTypeResolver;
import com.karifovas.gamerating.model.Adjustment;
import com.karifovas.gamerating.model.AdjustmentType;
import com.karifovas.gamerating.model.Factor;
import com.karifovas.gamerating.model.Rating;
import com.karifovas.gamerating.model.RatingConfiguration;
import com.karifovas.gamerating.model.RatingType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT
)
public interface RatingConfigurationMapper {

    @Mapping(target = "factorScale", source = "scale")
    @Mapping(target = "ratings", source = "ratings")
    RatingConfiguration map(ExternalRatingConfiguration externalConfiguration);

    RatingConfiguration.Scale toScale(ExternalRatingConfiguration.Scale scale);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "id")
    @Mapping(target = "gameId", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "value", ignore = true)
    @Mapping(target = "status", constant = "IDLE")
    @Mapping(target = "type", source = ".", qualifiedByName = "resolveRatingType")
    @Mapping(target = "drivingRatings", source = "calculation.sourceRatings")
    Rating toRating(ExternalRatingConfiguration.Rating externalRating);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "value", ignore = true)
    Factor toFactor(ExternalRatingConfiguration.Factor externalFactor);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", source = "id")
    @Mapping(target = "type", source = ".", qualifiedByName = "resolveAdjustmentType")
    @Mapping(target = "value", ignore = true)
    @Mapping(target = "valueScale", source = "valueRange")
    Adjustment toAdjustment(ExternalRatingConfiguration.Adjustment externalAdjustment);


    @Mapping(target = "ratingId", source = "ratingId")
    @Mapping(target = "weight", source = "weight")
    Rating.DrivingRating toDrivingRating(ExternalRatingConfiguration.SourceRating externalSourceRating);

    Adjustment.ValueScale toValueScale(ExternalRatingConfiguration.Scale externalScale);

    @Named("resolveAdjustmentType")
    default AdjustmentType resolveAdjustmentType(ExternalRatingConfiguration.Adjustment adjustment) {
        return AdjustmentType.resolveFromExternalConfiguration(adjustment.impact());
    }

    @Named("resolveRatingType")
    default RatingType resolveRatingType(ExternalRatingConfiguration.Rating rating) {
        return RatingTypeResolver.resolve(rating);
    }

}
