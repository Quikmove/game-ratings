package com.karifovas.gamerating.mapper;

import com.karifovas.gamerating.dto.ExternalRatingConfiguration;
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

    @Mapping(target = "gameId", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "value", ignore = true)
    @Mapping(target = "status", constant = "IDLE")
    @Mapping(target = "type", source = ".", qualifiedByName = "normalizeType")
    @Mapping(target = "drivingRatings", source = "calculation.sourceRatings")
    Rating toRating(ExternalRatingConfiguration.Rating externalRating);

    @Mapping(target = "name", source = "name")
    @Mapping(target = "value", ignore = true)
    Factor toFactor(ExternalRatingConfiguration.Factor externalFactor);

    @Mapping(target = "type", source = ".", qualifiedByName = "normalizeAdjustmentType")
    @Mapping(target = "value", ignore = true)
    @Mapping(target = "valueScale", source = "valueRange")
    Adjustment toAdjustment(ExternalRatingConfiguration.Adjustment externalAdjustment);

    @Mapping(target = "id", source = "ratingId")
    @Mapping(target = "weight", source = "weight")
    Rating.DrivingRating toDrivingRating(ExternalRatingConfiguration.SourceRating externalSourceRating);

    Adjustment.ValueScale toValueScale(ExternalRatingConfiguration.Scale externalScale);

    @Named("normalizeAdjustmentType")
    default AdjustmentType normalizeAdjustmentType(ExternalRatingConfiguration.Adjustment adjustment) {
        var externalType = adjustment.impact() != null ? adjustment.impact() : adjustment.type();
        if (externalType == null) {
            throw new RuntimeException("Adjustment type is missing");
        }

        return switch (externalType.toLowerCase()) {
            case "read_only" -> AdjustmentType.READ_ONLY;
            case "impactful", "affects_calculation" -> AdjustmentType.IMPACTFUL;
            default -> throw new RuntimeException("Unsupported adjustment type: %s".formatted(externalType));
        };
    }

    @Named("normalizeType")
    default RatingType normalizeType(ExternalRatingConfiguration.Rating rating) {
        if (rating.type() == null) {
            throw new RuntimeException("Rating type is missing");
        }
        if ("manual".equalsIgnoreCase(rating.type())) {
            return RatingType.MANUAL;
        }

        if (!"calculated".equalsIgnoreCase(rating.type())) {
            throw new RuntimeException("Unsupported external rating type: %s".formatted(rating.type()));
        }

        var calculation = rating.calculation();
        if (calculation == null || calculation.method() == null) {
            throw new RuntimeException("Type is \"calculated\", but calculation is not provided");
        }

        if ("rating-average".equalsIgnoreCase(calculation.method())) {
            if (calculation.sourceRatings() == null || calculation.sourceRatings().isEmpty()) {
                throw new RuntimeException("Type is calculated with rating-average, but sourceRatings are not provided");
            }
            return RatingType.RATING_FORMULA;
        }

        if ("factor_average".equalsIgnoreCase(calculation.method())) {
            return RatingType.WEIGHTED_AVERAGE;
        }


        throw new RuntimeException("Unsupported calculation method: %s".formatted(calculation.method()));
    }
}
