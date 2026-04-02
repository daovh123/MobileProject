package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceImportResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceImportServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Test
    void importFromFileShouldPersistNormalizedPlaces() throws Exception {
        PlaceImportService placeImportService = new PlaceImportService(placeRepository, new ObjectMapper());
        Path tempFile = Files.createTempFile("places-import-", ".json");

        String json = """
                {
                  "places": [
                    {
                      "id": " p1 ",
                      "name": " Pho Ong Cat ",
                      "address": " 201 Example Street ",
                      "district": " Quan 1 ",
                      "province": " TP. Ho Chi Minh ",
                      "isFood": true,
                      "isDrink": false,
                      "searchString": ""
                    },
                    {
                      "id": " ",
                      "name": "Invalid"
                    }
                  ]
                }
                """;

        Files.writeString(tempFile, json, StandardCharsets.UTF_8);
        when(placeRepository.count()).thenReturn(1L);

        PlaceImportResponse response = placeImportService.importFromFile(tempFile.toString(), false);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Place>> placesCaptor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository).saveAll(placesCaptor.capture());

        List<Place> savedPlaces = placesCaptor.getValue();
        assertEquals(1, savedPlaces.size());
        assertEquals("p1", savedPlaces.getFirst().getId());
        assertEquals("Pho Ong Cat", savedPlaces.getFirst().getName());
        assertNotNull(savedPlaces.getFirst().getSearchString());
        assertFalse(savedPlaces.getFirst().getSearchString().isBlank());

        assertEquals(1, response.importedCount());
        assertEquals(1L, response.totalInDatabase());
    }

    @Test
    void importFromFileShouldClearExistingDataWhenRequested() throws Exception {
        PlaceImportService placeImportService = new PlaceImportService(placeRepository, new ObjectMapper());
        Path tempFile = Files.createTempFile("places-import-clear-", ".json");

        String json = """
                {
                  "places": [
                    {
                      "id": "p1",
                      "name": "Pho Ong Cat"
                    }
                  ]
                }
                """;

        Files.writeString(tempFile, json, StandardCharsets.UTF_8);
        when(placeRepository.count()).thenReturn(1L);

        placeImportService.importFromFile(tempFile.toString(), true);

        verify(placeRepository).deleteAll();
    }
}