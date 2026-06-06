package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanRequest;
import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFilterOptionsResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFeatureSummaryResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceImageBackfillResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceImportResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller quản lý địa điểm (quán ăn, quán nước).
 * Base path: {@code /api/places}
 *
 * <p>Cung cấp các endpoint cho tìm kiếm, lọc, xem chi tiết, import dữ liệu,
 * backfill ảnh, và tạo kế hoạch khám phá (explore plan).</p>
 */
@Validated
@RestController
@RequestMapping("/api/places")
public class PlaceController {

    private final PlaceService placeService;
    private final ExplorePlanService explorePlanService;
    private final PlaceImportService placeImportService;
    private final PlaceImageBackfillService placeImageBackfillService;

    public PlaceController(
            PlaceService placeService,
            ExplorePlanService explorePlanService,
            PlaceImportService placeImportService,
            PlaceImageBackfillService placeImageBackfillService) {
        this.placeService = placeService;
        this.explorePlanService = explorePlanService;
        this.placeImportService = placeImportService;
        this.placeImageBackfillService = placeImageBackfillService;
    }

    /**
     * Import dữ liệu địa điểm từ file JSON.
     *
     * @param filePath         đường dẫn file JSON (mặc định: {@code app.place.import.file-path})
     * @param clearBeforeImport xóa toàn bộ dữ liệu cũ trước khi import
     * @return kết quả import (số lượng, tổng trong DB)
     */
    @PostMapping("/import")
    public ResponseEntity<PlaceImportResponse> importFromFile(
            @RequestParam(required = false) String filePath,
            @RequestParam(defaultValue = "false") boolean clearBeforeImport) {
        PlaceImportResponse response = placeImportService.importFromFile(filePath, clearBeforeImport);
        return ResponseEntity.ok(response);
    }

    /**
     * Backfill (thay thế) các ảnh GPS-CS không ổn định bằng ảnh từ pool nội bộ hoặc Wikimedia.
     *
     * @param dryRun nếu {@code true}, chỉ quét mà không lưu thay đổi
     * @param limit  số lượng địa điểm tối đa xử lý (1-20000, mặc định 5000)
     * @return kết quả backfill
     */
    @PostMapping("/backfill-images")
    public ResponseEntity<PlaceImageBackfillResponse> backfillImages(
            @RequestParam(defaultValue = "false") boolean dryRun,
            @RequestParam(defaultValue = "5000") @Min(1) @Max(20000) int limit) {
        PlaceImageBackfillResponse response = placeImageBackfillService.backfillGpsCsImages(dryRun, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * Tìm kiếm địa điểm với bộ lọc nâng cao.
     *
     * @param q        từ khóa tìm kiếm
     * @param province tỉnh/thành phố
     * @param district quận/huyện
     * @param type     loại: food, drink, all
     * @param minRating điểm đánh giá tối thiểu (0-5)
     * @param nearLat  vĩ độ hiện tại (để tính khoảng cách)
     * @param nearLng  kinh độ hiện tại
     * @param radiusKm bán kính tìm kiếm (km)
     * @param sort     cách sắp xếp: trending, rating, ratingAsc, ratingMix, distance
     * @param page     trang hiện tại (0-based)
     * @param size     số kết quả mỗi trang (1-100, mặc định 20)
     * @return danh sách địa điểm phân trang
     */
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

    /**
     * Lấy danh sách địa điểm trending.
     */
    @GetMapping("/trending")
    public ResponseEntity<PlaceSearchResponse> trending(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(placeService.trending(page, size));
    }

    /**
     * Lấy một địa điểm ngẫu nhiên theo bộ lọc.
     */
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

    /**
     * Lấy thống kê tổng quan features của dữ liệu địa điểm.
     */
    @GetMapping("/features")
    public ResponseEntity<PlaceFeatureSummaryResponse> featureSummary() {
        return ResponseEntity.ok(placeService.getFeatureSummary());
    }

    /**
     * Lấy danh sách quận/huyện và tỉnh/thành phố cho bộ lọc UI.
     */
    @GetMapping("/filter-options")
    public ResponseEntity<PlaceFilterOptionsResponse> filterOptions() {
        return ResponseEntity.ok(placeService.getFilterOptions());
    }

    /**
     * Xem chi tiết một địa điểm theo ID.
     */
    @GetMapping("/{placeId}")
    public ResponseEntity<PlaceDto> getDetail(@PathVariable String placeId) {
        return ResponseEntity.ok(placeService.findById(placeId));
    }

    /**
     * Tạo kế hoạch khám phá (explore plan) dựa trên ngân sách, sở thích và vị trí.
     * Tự động gợi ý các địa điểm ăn/uống xen kẽ, tối ưu theo budget.
     */
    @PostMapping("/explore-plan")
    public ResponseEntity<ExplorePlanResponse> buildExplorePlan(@Valid @RequestBody ExplorePlanRequest request) {
        return ResponseEntity.ok(explorePlanService.buildPlan(request));
    }
}
