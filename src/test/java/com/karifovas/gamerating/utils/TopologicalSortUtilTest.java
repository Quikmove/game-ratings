package com.karifovas.gamerating.utils;

import com.karifovas.gamerating.model.Rating;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
public class TopologicalSortUtilTest {

    private List<Rating> createTestRatings() {
        var rating1 = Rating.builder()
                .code("rating_1")
                .drivingRatings(List.of(
                        Rating.DrivingRating.builder().ratingCode("rating_3").weight(0.5f).build(),
                        Rating.DrivingRating.builder().ratingCode("rating_4").weight(0.5f).build()
                ))
                .build();

        var rating2 = Rating.builder()
                .code("rating_2")
                .drivingRatings(List.of(
                        Rating.DrivingRating.builder().ratingCode("rating_1").weight(0.25f).build(),
                        Rating.DrivingRating.builder().ratingCode("rating_4").weight(0.25f).build(),
                        Rating.DrivingRating.builder().ratingCode("rating_5").weight(0.5f).build()
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

        // When rating_3 changes, both rating_1 and rating_2 must be recalculated
        var affectedWhenRating3Changes = TopologicalSortUtil.findDependents(
                "rating_3",
                nodes,
                Rating::getCode,
                rating -> rating.getDrivingRatings().stream()
                        .map(Rating.DrivingRating::ratingCode)
                        .toList()
        );

        assertTrue(affectedWhenRating3Changes.contains("rating_3"), "Should include the root node");
        assertTrue(affectedWhenRating3Changes.contains("rating_1"), "rating_1 directly depends on rating_3");
        assertTrue(affectedWhenRating3Changes.contains("rating_2"), "rating_2 transitively depends on rating_3 (via rating_1)");
        assertEquals(3, affectedWhenRating3Changes.size(), "Only rating_3, rating_1, and rating_2 should be affected");

        // When rating_4 changes, all other ratings are affected
        var affectedWhenRating4Changes = TopologicalSortUtil.findDependents(
                "rating_4",
                nodes,
                Rating::getCode,
                rating -> rating.getDrivingRatings().stream()
                        .map(Rating.DrivingRating::ratingCode)
                        .toList()
        );

        assertTrue(affectedWhenRating4Changes.contains("rating_4"));
        assertTrue(affectedWhenRating4Changes.contains("rating_1"), "rating_1 depends on rating_4");
        assertTrue(affectedWhenRating4Changes.contains("rating_2"), "rating_2 depends on rating_4");
        assertEquals(3, affectedWhenRating4Changes.size(), "rating_4, rating_1, and rating_2 should be affected");

        // When rating_5 changes, only rating_2 is affected
        var affectedWhenRating5Changes = TopologicalSortUtil.findDependents(
                "rating_5",
                nodes,
                Rating::getCode,
                rating -> rating.getDrivingRatings().stream()
                        .map(Rating.DrivingRating::ratingCode)
                        .toList()
        );

        assertTrue(affectedWhenRating5Changes.contains("rating_5"));
        assertTrue(affectedWhenRating5Changes.contains("rating_2"), "rating_2 depends on rating_5");
        assertEquals(2, affectedWhenRating5Changes.size(), "Only rating_5 and rating_2 should be affected");
    }
}

