package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanRequest;
import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExplorePlanServiceTest {

    @Mock
    private PlaceService placeService;

    @Test
    void buildPlanShouldAlternateExperiencesWhenPossible() {
        ExplorePlanService service = new ExplorePlanService(placeService);
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(
                        place("food-1", "Pho Nha", "25k - 35k", true, false, 4.7, 120, "Pho"),
                        place("drink-1", "Tiem Cafe", "20k - 30k", false, true, 4.8, 300, "Ca phe"),
                        place("food-2", "Nuong Dem", "60k - 90k", true, false, 4.5, 220, "Nuong")
                ),
                3,
                0,
                200
        ));

        ExplorePlanResponse response = service.buildPlan(request(120_000L, 2, 2));

        assertEquals(2, response.items().size());
        assertEquals("food", response.items().getFirst().experienceType());
        assertEquals("drink", response.items().get(1).experienceType());
        assertFalse(response.summary().lowBalance());
    }

    @Test
    void buildPlanShouldFlagLowBalanceForTinyBudget() {
        ExplorePlanService service = new ExplorePlanService(placeService);
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(place("food-1", "Banh Mi", "15k", true, false, 4.1, 20, "Banh mi")),
                1,
                0,
                200
        ));

        ExplorePlanResponse response = service.buildPlan(request(20_000L, 2, 1));

        assertTrue(response.summary().lowBalance());
        assertEquals("Quy chung cua 2 ban con rat it, vui long nap them.", response.summary().balanceMessage());
        assertEquals(1, response.items().size());
    }

    @Test
    void buildPlanShouldSkipViewedGoneAndSentPlaceIds() {
        ExplorePlanService service = new ExplorePlanService(placeService);
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(
                        place("viewed-1", "Pho Nha", "25k - 35k", true, false, 4.7, 120, "Pho"),
                        place("gone-1", "Bun Bo Nho", "35k - 45k", true, false, 4.6, 80, "Bun bo"),
                        place("sent-1", "Tiem Tra Sua", "22k - 30k", false, true, 4.5, 200, "Tra sua"),
                        place("fresh-1", "Com Tam Moi", "40k - 50k", true, false, 4.8, 260, "Com tam")
                ),
                4,
                0,
                200
        ));

        ExplorePlanResponse response = service.buildPlan(new ExplorePlanRequest(
                null, null, null, "all", null, null, null, null,
                120_000L, 2, 1,
                List.of(),
                List.of("viewed-1"),
                List.of("gone-1"),
                List.of("sent-1"),
                List.of()
        ));

        assertEquals(1, response.items().size());
        assertEquals("fresh-1", response.items().getFirst().place().id());
    }

    @Test
    void buildPlanShouldAvoidRecentCuisineAndBrandWhenAlternativesExist() {
        ExplorePlanService service = new ExplorePlanService(placeService);
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(
                        place("pho-1", "Pho Thin", "35k - 45k", true, false, 4.9, 500, "Pho"),
                        place("bun-1", "Bun Cha 1986", "35k - 45k", true, false, 4.6, 250, "Bun cha"),
                        place("highlands-1", "Highlands Coffee Nguyen Hue", "45k", false, true, 4.8, 450, "Ca phe"),
                        place("phuc-long-1", "Phuc Long Garden", "45k", false, true, 4.7, 300, "Ca phe")
                ),
                4,
                0,
                200
        ));

        ExplorePlanResponse response = service.buildPlan(new ExplorePlanRequest(
                null, null, null, "all", null, null, null, null,
                150_000L, 2, 2,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of("Pho", "Highlands Coffee")
        ));

        assertEquals(2, response.items().size());
        assertEquals("bun-1", response.items().getFirst().place().id());
        assertEquals("phuc-long-1", response.items().get(1).place().id());
    }

    @Test
    void buildPlanShouldParseThousandSeparatedRanges() {
        ExplorePlanService service = new ExplorePlanService(placeService);
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(place("food-1", "Nuong", "30.000 - 50.000", true, false, 4.4, 120, "Nuong")),
                1,
                0,
                200
        ));

        ExplorePlanResponse response = service.buildPlan(request(500_000L, 2, 1));

        assertEquals(40_000L, response.items().getFirst().estimatedCost());
    }

    @Test
    void buildPlanShouldParseLowerBoundAndApproximatePerPersonPrices() {
        ExplorePlanService service = new ExplorePlanService(placeService);
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(
                        place("food-1", "Nuong", "tu 30k", true, false, 4.4, 120, "Nuong"),
                        place("drink-1", "Tea", "~50k/nguoi", false, true, 4.9, 99, "Tra sua")
                ),
                2,
                0,
                200
        ));

        ExplorePlanResponse foodResponse = service.buildPlan(request(500_000L, 2, 1));
        assertEquals(30_000L, foodResponse.items().getFirst().estimatedCost());

        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(place("drink-1", "Tea", "~50k/nguoi", false, true, 4.9, 99, "Tra sua")),
                1,
                0,
                200
        ));

        ExplorePlanResponse drinkResponse = service.buildPlan(new ExplorePlanRequest(
                null, null, null, "drink", null, null, null, null,
                500_000L, 2, 1,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        ));

        assertEquals(50_000L, drinkResponse.items().getFirst().estimatedCost());
    }

    @Test
    void buildPlanShouldNormalizeMultiPersonTotalPrices() {
        ExplorePlanService service = new ExplorePlanService(placeService);
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(place("food-1", "Lau Dem", "2 nguoi 200k", true, false, 4.5, 180, "Lau")),
                1,
                0,
                200
        ));

        ExplorePlanResponse response = service.buildPlan(request(500_000L, 2, 1));

        assertEquals(100_000L, response.items().getFirst().estimatedCost());
    }

    private ExplorePlanRequest request(long budget, int peopleCount, int desiredStops) {
        return new ExplorePlanRequest(
                null, null, null, "all", null, null, null, null,
                budget, peopleCount, desiredStops,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );
    }

    private PlaceDto place(String id, String name, String priceRange, boolean food, boolean drink,
                           double rating, int reviews, String tag) {
        return new PlaceDto(
                id,
                name,
                "Dia chi",
                "Quan 1",
                "Quan an",
                food ? "Mon chinh" : "Do uong",
                rating,
                reviews,
                null,
                priceRange,
                null,
                false,
                null,
                null,
                null,
                "TP HCM",
                null,
                tag,
                food,
                drink,
                null
        );
    }
}
