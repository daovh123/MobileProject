package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceImageBackfillResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceImageBackfillServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    @Mock
    private PlaceService placeService;

    @Mock
    private PlaceCacheService placeCacheService;

    private PlaceImageBackfillService backfillService;

    @BeforeEach
    void setUp() {
        backfillService = new PlaceImageBackfillService(placeRepository, placeService, placeCacheService);
    }

    @Test
    void backfillShouldReplaceAllDetectedGpsCsImagesInDryRunMode() {
        List<Place> gpsPlaces = List.of(
            place("p1", "https://lh3.googleusercontent.com/gps-cs-s/abc"),
            place("p2", "https://lh3.googleusercontent.com/gps-cs-s/def"),
            place("p3", "https://lh3.googleusercontent.com/gps-cs-s/ghi"));

        when(placeRepository.findByImageUrlContainingIgnoreCase("googleusercontent.com/gps-cs-s/"))
            .thenReturn(gpsPlaces);
        when(placeRepository.findByImageUrlIn(anyList())).thenReturn(List.of());
        when(placeRepository.findAll()).thenReturn(gpsPlaces);

        PlaceImageBackfillResponse response = backfillService.backfillGpsCsImages(true, 100);

        assertTrue(response.success());
        assertTrue(response.dryRun());
        assertEquals(3, response.scannedCount());
        assertEquals(3, response.totalGpsCsCount());
        assertEquals(3, response.replacedCount());
        verify(placeRepository, never()).saveAll(anyList());
        verify(placeService, never()).rebuildCache();
        verify(placeCacheService, never()).evictAll();
    }

    @Test
    void backfillShouldPersistUpdatedImagesAndRefreshCacheWhenNotDryRun() {
        List<Place> gpsPlaces = List.of(
            place("p1", "https://lh3.googleusercontent.com/gps-cs-s/abc"),
            place("p2", "https://lh3.googleusercontent.com/gps-cs-s/def"),
            place("p3", "https://lh3.googleusercontent.com/gps-cs-s/ghi"));

        List<Place> reusablePoolPlaces = List.of(
            place("ok-1", "https://lh3.googleusercontent.com/p/AF1QipPQUQmOiMn1kY_z18JvyYRrRhtiTvNgxw8EURpY=w1920-h1080"),
            place("ok-2", "https://lh3.googleusercontent.com/p/AF1QipPCg_nLGtjeze-2aZFm7Rms3yw-kUC2KWdl-d9Q=w1920-h1080"));

        when(placeRepository.findByImageUrlContainingIgnoreCase("googleusercontent.com/gps-cs-s/"))
            .thenReturn(gpsPlaces);
        when(placeRepository.findByImageUrlIn(anyList())).thenReturn(List.of());
        when(placeRepository.findAll()).thenReturn(reusablePoolPlaces);

        PlaceImageBackfillResponse response = backfillService.backfillGpsCsImages(false, 2);

        assertFalse(response.dryRun());
        assertEquals(2, response.scannedCount());
        assertEquals(3, response.totalGpsCsCount());
        assertEquals(2, response.replacedCount());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Place>> captor = ArgumentCaptor.forClass(List.class);
        verify(placeRepository, times(1)).saveAll(captor.capture());
        verify(placeService, times(1)).rebuildCache();
        verify(placeCacheService, times(1)).evictAll();

        List<Place> saved = captor.getValue();
        assertEquals(2, saved.size());
        assertTrue(saved.stream().allMatch(place -> place.getImageUrl() != null));
        assertTrue(saved.stream().allMatch(place -> place.getImageUrl().contains("/p/AF1Qip")));
    }

    private Place place(String id, String imageUrl) {
        Place place = new Place();
        place.setId(id);
        place.setImageUrl(imageUrl);
        return place;
    }
}
