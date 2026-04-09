package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceImageBackfillResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

@Service
public class PlaceImageBackfillService {

    private static final Logger logger = LoggerFactory.getLogger(PlaceImageBackfillService.class);

    private static final String GPS_CS_URL_MARKER = "googleusercontent.com/gps-cs-s/";
    private static final int DEFAULT_LIMIT = 5000;
    private static final int MAX_LIMIT = 20000;
    private static final int SAVE_BATCH_SIZE = 300;

    // Public Wikimedia CDN fallback pool used only when no reusable in-database image source exists.
    private static final List<String> WIKIMEDIA_FALLBACK_POOL = List.of(
            "https://upload.wikimedia.org/wikipedia/commons/thumb/0/0a/Little_Vietnam_Restaurant.jpg/1280px-Little_Vietnam_Restaurant.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f3/Nice_vietnamese_restaurant_3630.JPG/1280px-Nice_vietnamese_restaurant_3630.JPG",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/4/45/A_small_cup_of_coffee.JPG/1280px-A_small_cup_of_coffee.JPG",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c5/Roasted_coffee_beans.jpg/1280px-Roasted_coffee_beans.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b2/M%C3%B3n_%C4%83n_%C4%90%C3%B4ng_H%C3%A0%2C_T%E1%BA%BFt_2022_%28ph%E1%BB%9F_L%C3%BD_Qu%E1%BB%91c_s%C6%B0_%E1%BB%9F_c%C3%B4ng_vi%C3%AAn_C%E1%BB%8D_D%E1%BA%A7u%29_%282%29.jpg/960px-M%C3%B3n_%C4%83n_%C4%90%C3%B4ng_H%C3%A0%2C_T%E1%BA%BFt_2022_%28ph%E1%BB%9F_L%C3%BD_Qu%E1%BB%91c_s%C6%B0_%E1%BB%9F_c%C3%B4ng_vi%C3%AAn_C%E1%BB%8D_D%E1%BA%A7u%29_%282%29.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3d/Nam_pho_bowl.jpg/960px-Nam_pho_bowl.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/3/3a/Pho_in_Russia.jpg/960px-Pho_in_Russia.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ec/Ca_Phe_Sua_Da.jpg/960px-Ca_Phe_Sua_Da.jpg",
            "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e2/13-08-31-Kochtreffen-Wien-RalfR-N3S_7849-024.jpg/960px-13-08-31-Kochtreffen-Wien-RalfR-N3S_7849-024.jpg"
    );

    private final PlaceRepository placeRepository;
    private final PlaceService placeService;
    private final PlaceCacheService placeCacheService;

    public PlaceImageBackfillService(
            PlaceRepository placeRepository,
            PlaceService placeService,
            PlaceCacheService placeCacheService) {
        this.placeRepository = placeRepository;
        this.placeService = placeService;
        this.placeCacheService = placeCacheService;
    }

    public PlaceImageBackfillResponse backfillGpsCsImages(boolean dryRun, int limit) {
        int safeLimit = normalizeLimit(limit);

        List<Place> gpsCsPlaces = placeRepository.findByImageUrlContainingIgnoreCase(GPS_CS_URL_MARKER);
        List<Place> fallbackPlaces = placeRepository.findByImageUrlIn(WIKIMEDIA_FALLBACK_POOL);
        List<Place> targets = mergeTargets(gpsCsPlaces, fallbackPlaces, safeLimit);
        int totalGpsCsCount = gpsCsPlaces.size();

        List<String> replacementPool = buildReplacementPool();
        List<Place> updates = new ArrayList<>(targets.size());
        List<String> sampleUpdatedPlaceIds = new ArrayList<>();

        for (Place place : targets) {
            String replacementUrl = replacementUrlFor(place, replacementPool);
            if (!dryRun) {
                place.setImageUrl(replacementUrl);
                updates.add(place);
            }

            if (sampleUpdatedPlaceIds.size() < 20 && place.getId() != null) {
                sampleUpdatedPlaceIds.add(place.getId());
            }
        }

        if (!dryRun && !updates.isEmpty()) {
            saveInBatches(updates);
            placeService.rebuildCache();
            placeCacheService.evictAll();
        }

        String message = buildMessage(dryRun, targets.size(), totalGpsCsCount, replacementPool.size());
        logger.info(message);

        return new PlaceImageBackfillResponse(
                true,
                message,
                targets.size(),
                totalGpsCsCount,
                targets.size(),
                dryRun,
                List.copyOf(sampleUpdatedPlaceIds));
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private List<Place> mergeTargets(List<Place> gpsCsPlaces, List<Place> fallbackPlaces, int limit) {
        Map<String, Place> uniqueTargets = new LinkedHashMap<>();

        Stream.concat(gpsCsPlaces.stream(), fallbackPlaces.stream())
                .forEach(place -> {
                    if (place.getId() != null) {
                        uniqueTargets.putIfAbsent(place.getId(), place);
                    }
                });

        return uniqueTargets.values().stream().limit(limit).toList();
    }

    private List<String> buildReplacementPool() {
        Set<String> pool = new LinkedHashSet<>();
        for (Place place : placeRepository.findAll()) {
            String imageUrl = place.getImageUrl();
            if (isReusableImageUrl(imageUrl)) {
                pool.add(imageUrl);
            }
        }

        if (!pool.isEmpty()) {
            return List.copyOf(pool);
        }

        return WIKIMEDIA_FALLBACK_POOL;
    }

    private boolean isReusableImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return false;
        }

        if (imageUrl.toLowerCase().contains(GPS_CS_URL_MARKER)) {
            return false;
        }

        if (WIKIMEDIA_FALLBACK_POOL.contains(imageUrl)) {
            return false;
        }

        return PlaceImageUrlNormalizer.normalize(imageUrl) != null;
    }

    private String replacementUrlFor(Place place, List<String> replacementPool) {
        int index = Math.floorMod(Objects.hashCode(place.getId()), replacementPool.size());
        return replacementPool.get(index);
    }

    private void saveInBatches(List<Place> places) {
        for (int start = 0; start < places.size(); start += SAVE_BATCH_SIZE) {
            int end = Math.min(start + SAVE_BATCH_SIZE, places.size());
            placeRepository.saveAll(places.subList(start, end));
        }
    }

    private String buildMessage(boolean dryRun, int scannedCount, int totalGpsCsCount, int replacementPoolSize) {
        String mode = dryRun ? "Dry-run" : "Backfill";
        return mode + " completed: scanned=" + scannedCount
                + ", replaced=" + scannedCount
                + ", totalGpsCsFound=" + totalGpsCsCount
                + ", replacementPoolSize=" + replacementPoolSize;
    }
}
