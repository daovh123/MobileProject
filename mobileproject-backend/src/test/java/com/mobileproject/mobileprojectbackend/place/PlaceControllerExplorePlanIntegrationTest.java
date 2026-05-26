package com.mobileproject.mobileprojectbackend.place;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PlaceControllerExplorePlanIntegrationTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlaceService placeService = Mockito.mock(PlaceService.class);

    @BeforeEach
    void setUp() {
        ExplorePlanService explorePlanService = new ExplorePlanService(placeService);
        PlaceImportService placeImportService = Mockito.mock(PlaceImportService.class);
        PlaceImageBackfillService placeImageBackfillService = Mockito.mock(PlaceImageBackfillService.class);
        PlaceController controller = new PlaceController(
                placeService,
                explorePlanService,
                placeImportService,
                placeImageBackfillService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void explorePlanEndpointReturnsPlanItems() throws Exception {
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(
                        new PlaceDto("p1", "Pho", "Addr", "Q1", "Food", "Pho", 4.6, 100,
                                null, "25k - 35k", null, false, null, null, null,
                                "TP HCM", null, "Pho", true, false, null),
                        new PlaceDto("p2", "Cafe", "Addr", "Q1", "Drink", "Cafe", 4.7, 80,
                                null, "20k - 30k", null, false, null, null, null,
                                "TP HCM", null, "Ca phe", false, true, null)
                ),
                2,
                0,
                200
        ));

        ExplorePlanRequest request = new ExplorePlanRequest(
                null, null, null, null, "all", null, null, null, null,
                100_000L, 2, 2,
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of()
        );

        mockMvc.perform(post("/api/places/explore-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.totalBudget").value(100000))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].place.name").value("Pho"))
                .andExpect(jsonPath("$.items[1].place.name").value("Cafe"));
    }

    @Test
    void explorePlanEndpointAcceptsExplicitHistoryBuckets() throws Exception {
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(
                List.of(
                        new PlaceDto("viewed-1", "Pho Thin", "Addr", "Q1", "Food", "Pho", 4.8, 300,
                                null, "35k - 45k", null, false, null, null, null,
                                "TP HCM", null, "Pho", true, false, null),
                        new PlaceDto("fresh-1", "Bun Cha", "Addr", "Q1", "Food", "Bun cha", 4.6, 180,
                                null, "35k - 45k", null, false, null, null, null,
                                "TP HCM", null, "Bun cha", true, false, null)
                ),
                2,
                0,
                200
        ));

        String requestJson = """
                {
                  "budget": 100000,
                  "peopleCount": 2,
                  "desiredStops": 1,
                  "viewedPlaceIds": ["viewed-1"],
                  "gonePlaceIds": [],
                  "sentPlaceIds": [],
                  "recentKeywords": ["Pho"]
                }
                """;

        mockMvc.perform(post("/api/places/explore-plan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].place.id").value("fresh-1"));
    }
}
