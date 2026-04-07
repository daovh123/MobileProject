package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceImportResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class PlaceImportService {

    private static final Logger logger = LoggerFactory.getLogger(PlaceImportService.class);
    private static final int BATCH_SIZE = 500;

    private final PlaceRepository placeRepository;
    private final ObjectMapper objectMapper;
    private final PlaceService placeService;
    private final PlaceCacheService placeCacheService;

    @Value("${app.place.import.file-path:D:/data.json}")
    private String defaultImportFilePath;

    public PlaceImportService(
            PlaceRepository placeRepository,
            ObjectMapper objectMapper,
            PlaceService placeService,
            PlaceCacheService placeCacheService) {
        this.placeRepository = placeRepository;
        this.objectMapper = objectMapper;
        this.placeService = placeService;
        this.placeCacheService = placeCacheService;
    }

    public PlaceImportResponse importFromFile(String filePath, boolean clearBeforeImport) {
        String sourceFile = resolveSourceFile(filePath);
        Path path = Path.of(sourceFile);

        if (!Files.exists(path)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Import file not found: " + sourceFile);
        }

        try (InputStream inputStream = Files.newInputStream(path)) {
            JsonNode rootNode = objectMapper.readTree(inputStream);
            JsonNode placesNode = rootNode.path("places");

            if (!placesNode.isArray()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format: missing places array");
            }

            if (clearBeforeImport) {
                placeRepository.deleteAll();
                logger.info("Cleared existing places before import");
            }

            List<Place> places = new ArrayList<>();
            for (JsonNode placeNode : placesNode) {
                try {
                    Place place = objectMapper.treeToValue(placeNode, Place.class);
                    normalizePlace(place);
                    if (place.getId() == null || place.getId().isBlank()) {
                        continue;
                    }
                    places.add(place);
                } catch (Exception parseException) {
                    logger.warn("Skipped malformed place item: {}", parseException.getMessage());
                }
            }

            saveInBatches(places);

            // Keep in-memory and Redis caches consistent after import.
            placeService.invalidateCache();
            placeCacheService.evictAll();

            long totalInDb = placeRepository.count();
            String message = "Imported " + places.size() + " places from " + sourceFile;
            logger.info(message);

            return new PlaceImportResponse(true, message, places.size(), totalInDb, sourceFile);
        } catch (IOException ioException) {
            logger.error("Failed to import places from {}", sourceFile, ioException);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to import places file");
        }
    }

    private String resolveSourceFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return defaultImportFilePath;
        }
        return filePath.trim();
    }

    private void saveInBatches(List<Place> places) {
        for (int start = 0; start < places.size(); start += BATCH_SIZE) {
            int end = Math.min(start + BATCH_SIZE, places.size());
            placeRepository.saveAll(places.subList(start, end));
        }
    }

    private void normalizePlace(Place place) {
        place.setId(normalizeNullable(place.getId()));
        place.setName(normalizeNullable(place.getName()));
        place.setAddress(normalizeNullable(place.getAddress()));
        place.setDistrict(normalizeNullable(place.getDistrict()));
        place.setCategory(normalizeNullable(place.getCategory()));
        place.setMealType(normalizeNullable(place.getMealType()));
        place.setOpenHours(normalizeNullable(place.getOpenHours()));
        place.setPriceRange(normalizeNullable(place.getPriceRange()));
        place.setImageUrl(PlaceImageUrlNormalizer.normalize(place.getImageUrl()));
        place.setGoogleMapsUrl(normalizeNullable(place.getGoogleMapsUrl()));
        place.setProvince(normalizeNullable(place.getProvince()));
        place.setNormalizedDistrict(normalizeNullable(place.getNormalizedDistrict()));
        place.setEffectiveTag(normalizeNullable(place.getEffectiveTag()));

        if (place.getReviewCount() == null) {
            place.setReviewCount(0);
        }
        if (place.getIsPinned() == null) {
            place.setIsPinned(false);
        }
        if (place.getIsFood() == null) {
            place.setIsFood(false);
        }
        if (place.getIsDrink() == null) {
            place.setIsDrink(false);
        }
        if (place.getNormalizedDistrict() == null && place.getDistrict() != null) {
            place.setNormalizedDistrict(place.getDistrict());
        }

        String searchString = normalizeNullable(place.getSearchString());
        if (searchString == null || searchString.isBlank()) {
            searchString = buildSearchString(place);
        }
        place.setSearchString(searchString);
    }

    private String buildSearchString(Place place) {
        return String.join(" ",
                Objects.toString(place.getName(), ""),
                Objects.toString(place.getCategory(), ""),
                Objects.toString(place.getAddress(), ""),
                Objects.toString(place.getDistrict(), ""),
                Objects.toString(place.getProvince(), ""),
                Objects.toString(place.getEffectiveTag(), ""))
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isBlank() ? null : normalized;
    }
}