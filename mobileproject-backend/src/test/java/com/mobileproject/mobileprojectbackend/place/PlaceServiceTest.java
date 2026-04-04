package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFeatureSummaryResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                                place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true,
                                                10.76, 106.69, "pho ong cat quan 1"),
                                place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false,
                                                10.78, 106.68, "coffee date quan 3"),
                                place("p3", "Bun Bo", "TP. Ho Chi Minh", "Quan 1", true, false, 3.9, 100, false, 10.75,
                                                106.66, "bun bo quan 1")));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                "pho",
                                null,
                                null,
                                "food",
                                4.0,
                                null,
                                null,
                                null,
                                "trending",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(1, response.total());
                assertEquals("p1", response.items().getFirst().id());
        }

        @Test
        void searchShouldApplyCombinedFiltersTogether() {
                PlaceService placeService = new PlaceService(placeRepository);
                when(placeRepository.findAll()).thenReturn(List.of(
                                place("p1", "Pho Hoan Kiem", "Ha Noi", "Hoan Kiem", true, false, 4.5, 320, true,
                                                21.0280, 105.8345, "pho hoan kiem"),
                                place("p2", "Pho Rating Thap", "Ha Noi", "Dong Da", true, false, 3.8, 210, false,
                                                21.0300, 105.8320, "pho rating thap"),
                                place("p3", "Pho Sai Gon", "TP. Ho Chi Minh", "Quan 1", true, false, 4.7, 400, true,
                                                10.7750, 106.7000, "pho sai gon"),
                                place("p4", "Tra Chanh Hoan Kiem", "Ha Noi", "Hoan Kiem", false, true, 4.6, 190, false,
                                                21.0290, 105.8330, "tra chanh hoan kiem")));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                "pho",
                                "ha noi",
                                null,
                                "food",
                                4.0,
                                21.0278,
                                105.8342,
                                2.0,
                                "distance",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(1, response.total());
                assertEquals("p1", response.items().getFirst().id());
        }

        @Test
        void randomShouldRespectTypeFilter() {
                PlaceService placeService = new PlaceService(placeRepository);
                when(placeRepository.findAll()).thenReturn(List.of(
                                place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true,
                                                10.76, 106.69, "pho ong cat"),
                                place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false,
                                                10.78, 106.68, "coffee date")));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "drink",
                                null,
                                null,
                                null,
                                null,
                                "trending",
                                0,
                                1);

                PlaceDto response = placeService.random(request);

                assertEquals("p2", response.id());
                assertTrue(response.drink());
                assertFalse(response.food());
        }

        @Test
        void featureSummaryShouldAggregateCounts() {
                PlaceService placeService = new PlaceService(placeRepository);
                when(placeRepository.findAll()).thenReturn(List.of(
                                place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true,
                                                10.76, 106.69, "pho ong cat"),
                                place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false,
                                                10.78, 106.68, "coffee date"),
                                place("p3", "Bun Dau", "Ha Noi", "Hoan Kiem", true, false, 4.2, 120, false, null, null,
                                                "bun dau")));

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

                Place place1 = place("p1", "Pho Ong Cat", "TP. Ho Chi Minh", "Quan 1", true, false, 4.6, 500, true,
                                10.76, 106.69, "pho");
                place1.setNormalizedDistrict("Q. 1");

                Place place2 = place("p2", "Coffee Date", "TP. Ho Chi Minh", "Quan 3", false, true, 4.8, 220, false,
                                10.78, 106.68, "coffee");
                place2.setNormalizedDistrict("Q. 3");

                Place place3 = place("p3", "Bun Dau", "Ha Noi", "Hoan Kiem", true, false, 4.2, 120, false, null, null,
                                "bun dau");
                place3.setNormalizedDistrict("Q. Hoan Kiem");

                when(placeRepository.findAll()).thenReturn(List.of(place1, place2, place3));

                var response = placeService.getFilterOptions();

                assertTrue(response.districts().contains("Q. 1"));
                assertTrue(response.districts().contains("Q. 3"));
                assertTrue(response.provinces().contains("TP. Ho Chi Minh"));
                assertTrue(response.provinces().contains("Ha Noi"));
        }

        @Test
        void searchShouldFilterByAreaUsingDistrictWhenProvinceDoesNotMatch() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place placeInDistrict = place("p1", "Coffee Bien Hoa", "Dong Nai", "Bien Hoa", false, true,
                                4.4, 120, false, 10.95, 106.82, "coffee bien hoa");
                placeInDistrict.setNormalizedDistrict("Thanh pho Bien Hoa");

                Place placeOtherArea = place("p2", "Coffee Thu Duc", "TP. Ho Chi Minh", "Thu Duc", false, true,
                                4.6, 150, false, 10.85, 106.75, "coffee thu duc");

                when(placeRepository.findAll()).thenReturn(List.of(placeInDistrict, placeOtherArea));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                "bien hoa",
                                null,
                                "all",
                                null,
                                null,
                                null,
                                null,
                                "trending",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(1, response.total());
                assertEquals("p1", response.items().getFirst().id());
        }

        @Test
        void searchShouldMatchProvinceIgnoringVietnameseDiacritics() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place hanoiPlace = place("p1", "Pho Ha Noi", "Hà Nội", "Đống Đa", true, false,
                                4.5, 240, false, 21.03, 105.83, "pho ha noi");
                Place otherPlace = place("p2", "Pho Sai Gon", "TP. Ho Chi Minh", "Quan 1", true, false,
                                4.6, 300, false, 10.77, 106.69, "pho sai gon");

                when(placeRepository.findAll()).thenReturn(List.of(hanoiPlace, otherPlace));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                "ha noi",
                                null,
                                "all",
                                null,
                                null,
                                null,
                                null,
                                "trending",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(1, response.total());
                assertEquals("p1", response.items().getFirst().id());
        }

        @Test
        void searchShouldIncludeRatingEqualToThresholdForPlusRatingOptions() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place ratingFour = place("p1", "Place 4.0", "TP. Ho Chi Minh", "Quan 1", true, false,
                                4.0, 100, false, 10.77, 106.69, "place 4");
                Place ratingFive = place("p2", "Place 5.0", "TP. Ho Chi Minh", "Quan 3", true, false,
                                5.0, 80, false, 10.78, 106.68, "place 5");
                Place ratingLower = place("p3", "Place 3.9", "TP. Ho Chi Minh", "Quan 7", true, false,
                                3.9, 120, false, 10.73, 106.71, "place 3.9");

                when(placeRepository.findAll()).thenReturn(List.of(ratingFour, ratingFive, ratingLower));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "all",
                                4.0,
                                null,
                                null,
                                null,
                                "trending",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(2, response.total());
        }

        @Test
        void searchShouldDistributeResultsWhenRatingMixIsRequested() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place ratingFortyOne = place("p1", "Place 4.1", "TP. Ho Chi Minh", "Quan 1", true, false,
                                4.1, 140, false, 10.77, 106.69, "place 4.1");
                Place ratingFortyOneSecond = place("p4", "Place 4.1 second", "TP. Ho Chi Minh", "Quan 5", true, false,
                                4.1, 120, false, 10.75, 106.67, "place 4.1 second");
                Place ratingFortyNine = place("p2", "Place 4.9", "TP. Ho Chi Minh", "Quan 3", true, false,
                                4.9, 80, false, 10.78, 106.68, "place 4.9");
                Place ratingFive = place("p3", "Place 5.0", "TP. Ho Chi Minh", "Quan 7", true, false,
                                5.0, 120, false, 10.73, 106.71, "place 5.0");

                when(placeRepository.findAll()).thenReturn(List.of(
                                ratingFortyOne,
                                ratingFortyOneSecond,
                                ratingFortyNine,
                                ratingFive));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "all",
                                4.0,
                                null,
                                null,
                                null,
                                "ratingMix",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(4, response.total());
                assertEquals("p3", response.items().get(0).id());
                assertEquals("p2", response.items().get(1).id());
                assertEquals("p1", response.items().get(2).id());
                assertEquals("p4", response.items().get(3).id());
        }

        @Test
        void searchWithMinRatingOneShouldIncludePlacesWithoutRating() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place unrated = place("p1", "Unrated Place", "Ha Noi", "Dong Da", true, false,
                                null, 10, false, 21.02, 105.82, "unrated");
                Place rated = place("p2", "Rated Place", "Ha Noi", "Ba Dinh", true, false,
                                4.5, 120, false, 21.03, 105.83, "rated");

                when(placeRepository.findAll()).thenReturn(List.of(unrated, rated));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                "ha noi",
                                null,
                                "all",
                                1.0,
                                null,
                                null,
                                null,
                                "ratingMix",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(2, response.total());
        }

        @Test
        void searchWithMinRatingTwoShouldExcludePlacesWithoutRating() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place unrated = place("p1", "Unrated Place", "Ha Noi", "Dong Da", true, false,
                                null, 10, false, 21.02, 105.82, "unrated");
                Place rated = place("p2", "Rated Place", "Ha Noi", "Ba Dinh", true, false,
                                4.5, 120, false, 21.03, 105.83, "rated");

                when(placeRepository.findAll()).thenReturn(List.of(unrated, rated));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                "ha noi",
                                null,
                                "all",
                                2.0,
                                null,
                                null,
                                null,
                                "ratingMix",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(1, response.total());
                assertEquals("p2", response.items().getFirst().id());
        }

        @Test
        void searchShouldRejectInvalidTypeValue() {
                PlaceService placeService = new PlaceService(placeRepository);

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "dessert",
                                null,
                                null,
                                null,
                                null,
                                "trending",
                                0,
                                20);

                ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                                () -> placeService.search(request));

                assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode().value());
        }

        @Test
        void searchShouldRejectDistanceSortWithoutCoordinates() {
                PlaceService placeService = new PlaceService(placeRepository);

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "all",
                                null,
                                null,
                                null,
                                null,
                                "distance",
                                0,
                                20);

                ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                                () -> placeService.search(request));

                assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode().value());
        }

        @Test
        void searchShouldRejectRadiusWithoutCoordinates() {
                PlaceService placeService = new PlaceService(placeRepository);

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "all",
                                null,
                                null,
                                null,
                                5.0,
                                "trending",
                                0,
                                20);

                ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                                () -> placeService.search(request));

                assertEquals(HttpStatus.BAD_REQUEST.value(), exception.getStatusCode().value());
        }

        @Test
        void searchShouldFilterByRadiusWhenCoordinatesProvided() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place nearPlace = place("p1", "Near Place", "TP. Ho Chi Minh", "Quan 1", true, false,
                                4.6, 120, false, 10.7760, 106.6990, "near place");
                Place farPlace = place("p2", "Far Place", "TP. Ho Chi Minh", "Quan 3", true, false,
                                4.9, 240, false, 10.9000, 106.8200, "far place");

                when(placeRepository.findAll()).thenReturn(List.of(nearPlace, farPlace));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "all",
                                null,
                                10.7780,
                                106.7000,
                                5.0,
                                "distance",
                                0,
                                20);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(1, response.total());
                assertEquals("p1", response.items().getFirst().id());
                assertTrue(response.items().getFirst().distanceKm() != null);
                assertTrue(response.items().getFirst().distanceKm() <= 5.0);
        }

        @Test
        void searchShouldApplyPagingAfterFiltering() {
                PlaceService placeService = new PlaceService(placeRepository);

                Place top = place("p1", "Top", "TP. Ho Chi Minh", "Quan 1", true, false,
                                5.0, 200, false, 10.77, 106.69, "top");
                Place middle = place("p2", "Middle", "TP. Ho Chi Minh", "Quan 3", true, false,
                                4.5, 180, false, 10.78, 106.68, "middle");
                Place low = place("p3", "Low", "TP. Ho Chi Minh", "Quan 7", true, false,
                                4.0, 160, false, 10.74, 106.71, "low");

                when(placeRepository.findAll()).thenReturn(List.of(top, middle, low));

                PlaceSearchRequest request = new PlaceSearchRequest(
                                null,
                                null,
                                null,
                                "all",
                                null,
                                null,
                                null,
                                null,
                                "trending",
                                1,
                                1);

                PlaceSearchResponse response = placeService.search(request);

                assertEquals(3, response.total());
                assertEquals(1, response.items().size());
                assertEquals("p2", response.items().getFirst().id());
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