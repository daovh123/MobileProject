package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFilterOptionsResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFeatureSummaryResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceImageBackfillResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceImportResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;
    private final PlaceImportService placeImportService;
    private final PlaceImageBackfillService placeImageBackfillService;

    public PlaceController(
            PlaceService placeService,
            PlaceImportService placeImportService,
            PlaceImageBackfillService placeImageBackfillService) {
        this.placeService = placeService;
        this.placeImportService = placeImportService;
        this.placeImageBackfillService = placeImageBackfillService;
    }

    @PostMapping("/import")
    public ResponseEntity<PlaceImportResponse> importFromFile(
            @RequestParam(required = false) String filePath,
            @RequestParam(defaultValue = "false") boolean clearBeforeImport) {
        PlaceImportResponse response = placeImportService.importFromFile(filePath, clearBeforeImport);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/backfill-images")
    public ResponseEntity<PlaceImageBackfillResponse> backfillImages(
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestParam(defaultValue = "5000") @Min(1) @Max(20000) int limit) {
        PlaceImageBackfillResponse response = placeImageBackfillService.backfillGpsCsImages(dryRun, limit);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PlaceSearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double nearLat,
            @RequestParam(required = false) Double nearLng,
            @RequestParam(required = false) Double radiusKm,
            @RequestParam(defaultValue = "trending") String sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        PlaceSearchRequest request = new PlaceSearchRequest(
                q,
                province,
                district,
                type,
                minRating,
                nearLat,
                nearLng,
                radiusKm,
                sort,
                page,
                size);
        return ResponseEntity.ok(placeService.search(request));
    }

    @GetMapping("/trending")
    public ResponseEntity<PlaceSearchResponse> trending(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(placeService.trending(page, size));
    }

    @GetMapping("/random")
    public ResponseEntity<PlaceDto> random(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String district,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Double nearLat,
            @RequestParam(required = false) Double nearLng,
            @RequestParam(required = false) Double radiusKm) {
        PlaceSearchRequest request = new PlaceSearchRequest(
                q,
                province,
                district,
                type,
                minRating,
                nearLat,
                nearLng,
                radiusKm,
                "trending",
                0,
                1);
        return ResponseEntity.ok(placeService.random(request));
    }

    @GetMapping("/features")
    public ResponseEntity<PlaceFeatureSummaryResponse> featureSummary() {
        return ResponseEntity.ok(placeService.getFeatureSummary());
    }

    @GetMapping("/filter-options")
    public ResponseEntity<PlaceFilterOptionsResponse> filterOptions() {
        return ResponseEntity.ok(placeService.getFilterOptions());
    }

    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDto> getDetail(@PathVariable String placeId) {
        return ResponseEntity.ok(placeService.findById(placeId));
    }
}