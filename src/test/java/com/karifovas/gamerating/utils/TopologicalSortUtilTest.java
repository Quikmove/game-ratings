package com.karifovas.gamerating.utils;

import com.karifovas.gamerating.model.Rating;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
public class TopologicalSortUtilTest {

    private List<Rating> createTestRatings() {
        var rating1 = Rating.builder()
                .code("rating_1")
                .drivingRatings(List.of(
                        Rating.DrivingRating.builder().ratingCode("rating_3").weight(BigDecimal.valueOf(0.5)).build(),
                        Rating.DrivingRating.builder().ratingCode("rating_4").weight(BigDecimal.valueOf(0.5)).build()
                ))
                .build();

        var rating2 = Rating.builder()
                .code("rating_2")
                .drivingRatings(List.of(
                        Rating.DrivingRating.builder().ratingCode("rating_1").weight(BigDecimal.valueOf(0.25)).build(),
                        Rating.DrivingRating.builder().ratingCode("rating_4").weight(BigDecimal.valueOf(0.25)).build(),
                        Rating.DrivingRating.builder().ratingCode("rating_5").weight(BigDecimal.valueOf(0.5)).build()
                ))
                .build();

        return List.of(
                rating1,
                rating2,
                Rating.builder().code("rating_3").drivingRatings(List.of()).build(),
                Rating.builder().code("rating_4").drivingRatings(List.of()).build(),
                Rating.builder().code("rating_5").drivingRatings(List.of()).build()
        );
    }

    private Rating ratingByCode(List<Rating> ratings, String code) {
        return ratings.stream()
                .filter(rating -> code.equals(rating.getCode()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing test rating: " + code));
    }

    private List<Rating> directDependents(List<Rating> ratings, Rating rating) {
        return ratings.stream()
                .filter(candidate -> candidate.getDrivingRatings().stream()
                        .anyMatch(drivingRating -> rating.getCode().equals(drivingRating.ratingCode())))
                .toList();
    }

    private Set<String> codesOf(Set<Rating> ratings) {
        return ratings.stream()
                .map(Rating::getCode)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Test
    void correctOrderWhenTopologicalSort() {
        var nodes = createTestRatings();

        var order = TopologicalSortUtil.sort(
                nodes,
                Rating::getCode,
                rating -> rating.getDrivingRatings().stream()
                        .map(Rating.DrivingRating::ratingCode)
                        .toList()
        );

        assertEquals(5, order.size());

        List<String> codes = order.stream().map(Rating::getCode).toList();
        order.forEach(rating ->
            rating.getDrivingRatings().forEach(drivingRating -> {
                int depIndex = codes.indexOf(drivingRating.ratingCode());
                int currentIndex = codes.indexOf(rating.getCode());
                assertTrue(depIndex < currentIndex,
                        "Dependency '" + drivingRating.ratingCode() + "' should appear before '" + rating.getCode() + "'");
            })
        );
    }

    @Test
    void throwsWhenDuplicateNodeIds() {
        var duplicateNodes = List.of(
                Rating.builder().code("rating_1").drivingRatings(List.of()).build(),
                Rating.builder().code("rating_1").drivingRatings(List.of()).build()
        );

        var exception = assertThrows(IllegalArgumentException.class, () ->
                TopologicalSortUtil.sort(duplicateNodes, Rating::getCode,
                        rating -> rating.getDrivingRatings().stream()
                                .map(Rating.DrivingRating::ratingCode)
                                .toList())
        );

        assertTrue(exception.getMessage().contains("rating_1"), "Error message should include the duplicate ID");
    }

    @Test
    void findDependentsWhenNodeChanges() {
        var nodes = createTestRatings();
        var rating3 = ratingByCode(nodes, "rating_3");
        var rating4 = ratingByCode(nodes, "rating_4");
        var rating5 = ratingByCode(nodes, "rating_5");

        // When rating_3 changes, both rating_1 and rating_2 must be recalculated
        var affectedWhenRating3Changes = TopologicalSortUtil.findDependents(
                rating3,
                nodes,
                Rating::getCode,
                rating -> directDependents(nodes, rating)
        );

        var affectedWhenRating3Codes = codesOf(affectedWhenRating3Changes);

        assertTrue(affectedWhenRating3Codes.contains("rating_3"), "Should include the root node");
        assertTrue(affectedWhenRating3Codes.contains("rating_1"), "rating_1 directly depends on rating_3");
        assertTrue(affectedWhenRating3Codes.contains("rating_2"), "rating_2 transitively depends on rating_3 (via rating_1)");
        assertEquals(3, affectedWhenRating3Changes.size(), "Only rating_3, rating_1, and rating_2 should be affected");

        // When rating_4 changes, only rating_4 and its dependents (rating_1 and rating_2) are affected
        var affectedWhenRating4Changes = TopologicalSortUtil.findDependents(
                rating4,
                nodes,
                Rating::getCode,
                rating -> directDependents(nodes, rating)
        );

        var affectedWhenRating4Codes = codesOf(affectedWhenRating4Changes);

        assertTrue(affectedWhenRating4Codes.contains("rating_4"));
        assertTrue(affectedWhenRating4Codes.contains("rating_1"), "rating_1 depends on rating_4");
        assertTrue(affectedWhenRating4Codes.contains("rating_2"), "rating_2 depends on rating_4");
        assertEquals(3, affectedWhenRating4Changes.size(), "rating_4, rating_1, and rating_2 should be affected");

        // When rating_5 changes, only rating_2 is affected
        var affectedWhenRating5Changes = TopologicalSortUtil.findDependents(
                rating5,
                nodes,
                Rating::getCode,
                rating -> directDependents(nodes, rating)
        );

        var affectedWhenRating5Codes = codesOf(affectedWhenRating5Changes);

        assertTrue(affectedWhenRating5Codes.contains("rating_5"));
        assertTrue(affectedWhenRating5Codes.contains("rating_2"), "rating_2 depends on rating_5");
        assertEquals(2, affectedWhenRating5Changes.size(), "Only rating_5 and rating_2 should be affected");
    }
}
