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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceImportServiceTest {

  @Mock
  private PlaceRepository placeRepository;

  @Mock
  private PlaceService placeService;

  @Mock
  private PlaceCacheService placeCacheService;

  @Test
  void importFromFileShouldPersistNormalizedPlaces() throws Exception {
    PlaceImportService placeImportService = new PlaceImportService(
        placeRepository,
        new ObjectMapper(),
        placeService,
        placeCacheService);
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
    verify(placeService).invalidateCache();
    verify(placeCacheService).evictAll();
  }

  @Test
  void importFromFileShouldClearExistingDataWhenRequested() throws Exception {
    PlaceImportService placeImportService = new PlaceImportService(
        placeRepository,
        new ObjectMapper(),
        placeService,
        placeCacheService);
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

  @Test
  void importFromFileShouldNormalizeImageUrls() throws Exception {
    PlaceImportService placeImportService = new PlaceImportService(
        placeRepository,
        new ObjectMapper(),
        placeService,
        placeCacheService);
    Path tempFile = Files.createTempFile("places-import-image-url-", ".json");

    String json = """
        {
          "places": [
            {
              "id": "p1",
              "name": "Place 1",
              "imageUrl": "N/A"
            },
            {
              "id": "p2",
              "name": "Place 2",
              "imageUrl": "//lh3.googleusercontent.com/p/AF1QipTest=w1920-h1080"
            },
            {
              "id": "p3",
              "name": "Place 3",
              "imageUrl": "www.example.com/photo.jpg"
            },
            {
              "id": "p4",
              "name": "Place 4",
              "imageUrl": "https://maps.app.goo.gl/N4X9iSFLg5tRCM1P9"
            }
          ]
        }
        """;

    Files.writeString(tempFile, json, StandardCharsets.UTF_8);
    when(placeRepository.count()).thenReturn(4L);

    placeImportService.importFromFile(tempFile.toString(), false);

    @SuppressWarnings("unchecked")
    ArgumentCaptor<List<Place>> placesCaptor = ArgumentCaptor.forClass(List.class);
    verify(placeRepository).saveAll(placesCaptor.capture());

    List<Place> savedPlaces = placesCaptor.getValue();

    assertEquals(4, savedPlaces.size());
    assertNull(savedPlaces.get(0).getImageUrl());
    assertEquals("https://lh3.googleusercontent.com/p/AF1QipTest=w1920-h1080", savedPlaces.get(1).getImageUrl());
    assertEquals("https://www.example.com/photo.jpg", savedPlaces.get(2).getImageUrl());
    assertNull(savedPlaces.get(3).getImageUrl());
  }
}