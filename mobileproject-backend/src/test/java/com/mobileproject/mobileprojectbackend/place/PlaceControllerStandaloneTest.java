package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceImageBackfillResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceImportResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PlaceControllerStandaloneTest {

    @Mock
    private PlaceService placeService;

    @Mock
    private PlaceImportService placeImportService;

        @Mock
        private PlaceImageBackfillService placeImageBackfillService;

    @Captor
    private ArgumentCaptor<PlaceSearchRequest> searchRequestCaptor;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
                PlaceController controller = new PlaceController(placeService, placeImportService, placeImageBackfillService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void searchShouldMapAllFilterParamsToSearchRequest() throws Exception {
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(List.of(), 0, 0, 20));

        mockMvc.perform(get("/api/places")
                .param("q", "pho")
                .param("province", "ha noi")
                .param("district", "dong da")
                .param("type", "food")
                .param("minRating", "4")
                .param("nearLat", "21.028")
                .param("nearLng", "105.834")
                .param("radiusKm", "3")
                .param("sort", "distance")
                .param("page", "1")
                .param("size", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0));

        verify(placeService).search(searchRequestCaptor.capture());
        PlaceSearchRequest captured = searchRequestCaptor.getValue();

        assertEquals("pho", captured.q());
        assertEquals("ha noi", captured.province());
        assertEquals("dong da", captured.district());
        assertEquals("food", captured.type());
        assertEquals(4.0, captured.minRating());
        assertEquals(21.028, captured.nearLat());
        assertEquals(105.834, captured.nearLng());
        assertEquals(3.0, captured.radiusKm());
        assertEquals("distance", captured.sort());
        assertEquals(1, captured.page());
        assertEquals(15, captured.size());
    }

    @Test
    void searchShouldUseDefaultValuesWhenParamsMissing() throws Exception {
        when(placeService.search(any())).thenReturn(new PlaceSearchResponse(List.of(), 0, 0, 20));

        mockMvc.perform(get("/api/places"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20));

        verify(placeService).search(searchRequestCaptor.capture());
        PlaceSearchRequest captured = searchRequestCaptor.getValue();

        assertEquals("all", captured.type());
        assertEquals("trending", captured.sort());
        assertEquals(0, captured.page());
        assertEquals(20, captured.size());
    }

    @Test
    void randomShouldMapFilterParamsToSearchRequest() throws Exception {
        when(placeService.random(any())).thenReturn(new PlaceDto(
                "p1",
                "Pho Random",
                "123 Street",
                "Dong Da",
                "Quan an",
                "Bua trua",
                4.7,
                321,
                "07:00 - 21:00",
                "50000-120000",
                null,
                false,
                null,
                21.028,
                105.834,
                "Ha Noi",
                "Dong Da",
                "Pho",
                true,
                false,
                1.2));

        mockMvc.perform(get("/api/places/random")
                .param("q", "pho")
                .param("type", "food")
                .param("minRating", "4")
                .param("nearLat", "21.028")
                .param("nearLng", "105.834")
                .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("p1"));

        verify(placeService).random(searchRequestCaptor.capture());
        PlaceSearchRequest captured = searchRequestCaptor.getValue();

        assertEquals("pho", captured.q());
        assertEquals("food", captured.type());
        assertEquals(4.0, captured.minRating());
        assertEquals(21.028, captured.nearLat());
        assertEquals(105.834, captured.nearLng());
        assertEquals(5.0, captured.radiusKm());
        assertEquals("trending", captured.sort());
        assertEquals(0, captured.page());
        assertEquals(1, captured.size());
    }

    @Test
    void importShouldForwardFlagsToImportService() throws Exception {
        when(placeImportService.importFromFile(eq("D:/data.json"), eq(true)))
                .thenReturn(new PlaceImportResponse(true, "ok", 10, 10, "D:/data.json"));

        mockMvc.perform(post("/api/places/import")
                .param("filePath", "D:/data.json")
                .param("clearBeforeImport", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.importedCount").value(10));

        verify(placeImportService).importFromFile("D:/data.json", true);
    }

        @Test
        void backfillImagesShouldForwardParamsToService() throws Exception {
                when(placeImageBackfillService.backfillGpsCsImages(eq(false), eq(2000)))
                                .thenReturn(new PlaceImageBackfillResponse(
                                                true,
                                                "ok",
                                                2000,
                                                3124,
                                                2000,
                                                false,
                                                List.of("p1")));

                mockMvc.perform(post("/api/places/backfill-images")
                                .param("dryRun", "false")
                                .param("limit", "2000"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.replacedCount").value(2000));

                verify(placeImageBackfillService).backfillGpsCsImages(false, 2000);
        }

    @Test
    void searchShouldPropagateBadRequestFromService() throws Exception {
        when(placeService.search(any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type value"));

        mockMvc.perform(get("/api/places")
                .param("type", "invalid"))
                .andExpect(status().isBadRequest());
    }
}
