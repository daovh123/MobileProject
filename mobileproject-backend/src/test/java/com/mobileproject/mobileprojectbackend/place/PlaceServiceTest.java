package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFeatureSummaryResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void searchShouldFilterByQueryTypeAndRating() {
        PlaceService placeService = new PlaceService(placeRepository);
        when(placeRepository.findAll()).thenReturn(List.of(
                place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true, 10.76, 106.69, "pho ong cat quan 1"),
                place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false, 10.78, 106.68, "coffee date quan 3"),
                place("p3", "Bun Bo", "TP. Ho Chi Minh", "Quan 1", true, false, 3.9, 100, false, 10.75, 106.66, "bun bo quan 1")
        ));

        PlaceSearchRequest request = new PlaceSearchRequest(
                "pho",
                null,
                null,
                "food",
                4.0,
                false,
                null,
                null,
                null,
                "trending",
                0,
                20
        );

        PlaceSearchResponse response = placeService.search(request);

        assertEquals(1, response.total());
        assertEquals("p1", response.items().getFirst().id());
    }

    @Test
    void randomShouldRespectTypeFilter() {
        PlaceService placeService = new PlaceService(placeRepository);
        when(placeRepository.findAll()).thenReturn(List.of(
                place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true, 10.76, 106.69, "pho ong cat"),
                place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false, 10.78, 106.68, "coffee date")
        ));

        PlaceSearchRequest request = new PlaceSearchRequest(
                null,
                null,
                null,
                "drink",
                null,
                false,
                null,
                null,
                null,
                "trending",
                0,
                1
        );

        PlaceDto response = placeService.random(request);

        assertEquals("p2", response.id());
        assertTrue(response.drink());
        assertFalse(response.food());
    }

    @Test
    void featureSummaryShouldAggregateCounts() {
        PlaceService placeService = new PlaceService(placeRepository);
        when(placeRepository.findAll()).thenReturn(List.of(
                place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true, 10.76, 106.69, "pho ong cat"),
                place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false, 10.78, 106.68, "coffee date"),
                place("p3", "Bun Dau", "Ha Noi", "Hoan Kiem", true, false, 4.2, 120, false, null, null, "bun dau")
        ));

        PlaceFeatureSummaryResponse response = placeService.getFeatureSummary();

        assertEquals(3, response.totalPlaces());
        assertEquals(2, response.totalFoodPlaces());
        assertEquals(1, response.totalDrinkPlaces());
        assertEquals(1, response.totalPinnedPlaces());
        assertEquals(2, response.totalPlacesWithCoordinates());
        assertEquals("tp. ho chi minh", response.topProvinces().getFirst().name());
    }

        @Test
        void filterOptionsShouldReturnDistinctDistrictsAndProvinces() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place place1 = place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true, 10.76, 106.69, "pho");
                place1.setNormalizedDistrict("Q. 1");

                Place place2 = place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false, 10.78, 106.68, "coffee");
                place2.setNormalizedDistrict("Q. 3");

                Place place3 = place("p3", "Bun Dau", "Ha Noi", "Hoan Kiem", true, false, 4.2, 120, false, null, null, "bun dau");
                place3.setNormalizedDistrict("Q. Hoan Kiem");

                when(placeRepository.findAll()).thenReturn(List.of(place1, place2, place3));

                var response = placeService.getFilterOptions();

                assertTrue(response.districts().contains("Q. 1"));
                assertTrue(response.districts().contains("Q. 3"));
                assertTrue(response.provinces().contains("TP. Ho Chi Minh"));
                assertTrue(response.provinces().contains("Ha Noi"));
        }

    private Place place(String id,
                        String name,
                        String province,
                        String district,
                        boolean isFood,
                        boolean isDrink,
                        Double rating,
                        Integer reviewCount,
                        boolean isPinned,
                        Double lat,
                        Double lng,
                        String searchString) {
        Place place = new Place();
        place.setId(id);
        place.setName(name);
        place.setProvince(province);
        place.setDistrict(district);
        place.setNormalizedDistrict(district);
        place.setIsFood(isFood);
        place.setIsDrink(isDrink);
        place.setRating(rating);
        place.setReviewCount(reviewCount);
        place.setIsPinned(isPinned);
        place.setLat(lat);
        place.setLng(lng);
        place.setSearchString(searchString);
        place.setEffectiveTag("tag");
        return place;
    }
}