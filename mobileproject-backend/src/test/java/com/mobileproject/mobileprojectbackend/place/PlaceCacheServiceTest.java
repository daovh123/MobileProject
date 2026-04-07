package com.mobileproject.mobileprojectbackend.place;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceCacheServiceTest {

    @Mock
    private PlaceRepository placeRepository;

    private PlaceCacheService placeCacheService;

    @BeforeEach
    void setUp() {
        placeCacheService = new PlaceCacheService(placeRepository, new ConcurrentMapCacheManager("placeById"));
    }

    @Test
    void findByIdShouldUseCacheAfterFirstLoad() {
        Place place = createPlace("p1", "Pho 24");
        when(placeRepository.findById("p1")).thenReturn(Optional.of(place));

        Optional<Place> first = placeCacheService.findById("p1");
        Optional<Place> second = placeCacheService.findById("p1");

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertEquals("p1", second.get().getId());
        verify(placeRepository, times(1)).findById("p1");
    }

    @Test
    void findAllByIdsShouldLoadMissingOnceAndKeepInputOrder() {
        Place p1 = createPlace("p1", "Pho 24");
        Place p2 = createPlace("p2", "Bun Bo");
        when(placeRepository.findAllById(List.of("p1", "p2"))).thenReturn(List.of(p1, p2));

        Map<String, Place> first = placeCacheService.findAllByIds(List.of("p1", "p2", "p1"));
        Map<String, Place> second = placeCacheService.findAllByIds(List.of("p2", "p1"));

        assertEquals(List.of("p1", "p2"), new ArrayList<>(first.keySet()));
        assertEquals("Bun Bo", second.get("p2").getName());
        verify(placeRepository, times(1)).findAllById(List.of("p1", "p2"));
    }

    @Test
    void evictAllShouldClearCacheAndForceReload() {
        Place place = createPlace("p1", "Pho 24");
        when(placeRepository.findById("p1")).thenReturn(Optional.of(place));

        placeCacheService.findById("p1");
        placeCacheService.evictAll();
        placeCacheService.findById("p1");

        verify(placeRepository, times(2)).findById("p1");
    }

    private Place createPlace(String id, String name) {
        Place place = new Place();
        place.setId(id);
        place.setName(name);
        return place;
    }
}
