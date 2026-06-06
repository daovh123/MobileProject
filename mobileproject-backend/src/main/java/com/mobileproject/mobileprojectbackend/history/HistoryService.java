package com.mobileproject.mobileprojectbackend.history;

import com.mobileproject.mobileprojectbackend.history.dto.HistoryListResponse;
import com.mobileproject.mobileprojectbackend.place.Place;
import com.mobileproject.mobileprojectbackend.place.PlaceCacheService;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Service xử lý lịch sử xem địa điểm.
 *
 * <p><strong>Business logic:</strong></p>
 * <ul>
 *   <li>Ghi nhận lượt xem: xác thực place tồn tại qua cache → lưu bản ghi mới</li>
 *   <li>Lấy lịch sử: query theo userId → batch load place từ cache → trả về danh sách PlaceDto</li>
 *   <li>Xóa lịch sử: xóa toàn bộ bản ghi theo userId</li>
 * </ul>
 *
 * <p>Sử dụng {@link PlaceCacheService} để load thông tin địa điểm từ cache,
 * tránh N+1 query MongoDB.</p>
 */
@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UserHistoryRepository historyRepository;
    private final PlaceCacheService placeCacheService;

    /**
     * Ghi nhận lượt xem địa điểm.
     *
     * @param userId  ID người xem
     * @param placeId ID địa điểm
     * @return kết quả {"success": true}
     * @throws ResponseStatusException 404 nếu place không tồn tại
     */
    public Map<String, Object> recordView(String userId, String placeId) {
        if (placeCacheService.findById(placeId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }

        historyRepository.save(new UserHistory(userId, placeId));
        return Map.of("success", true, "message", "View recorded");
    }

    /**
     * Lấy lịch sử xem địa điểm của user (mới nhất trước).
     */
    public HistoryListResponse getUserHistory(String userId) {
        List<UserHistory> histories = historyRepository.findByUserIdOrderByViewedAtDesc(userId);
        List<String> placeIds = histories.stream().map(UserHistory::getPlaceId).toList();

        Map<String, Place> placeById = placeCacheService.findAllByIds(placeIds);

        List<PlaceDto> places = placeIds.stream()
                .map(placeById::get)
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();

        return new HistoryListResponse(true, "History retrieved", places);
    }

    /**
     * Xóa toàn bộ lịch sử xem của user.
     */
    public Map<String, Object> clearHistory(String userId) {
        historyRepository.deleteByUserId(userId);
        return Map.of("success", true, "message", "History cleared");
    }

    private PlaceDto toDto(Place place) {
        return new PlaceDto(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getDistrict(),
                place.getCategory(),
                place.getMealType(),
                place.getRating(),
                place.getReviewCount(),
                place.getOpenHours(),
                place.getPriceRange(),
                place.getImageUrl(),
                Boolean.TRUE.equals(place.getIsPinned()),
                place.getGoogleMapsUrl(),
                place.getLat(),
                place.getLng(),
                place.getProvince(),
                place.getNormalizedDistrict(),
                place.getEffectiveTag(),
                Boolean.TRUE.equals(place.getIsFood()),
                Boolean.TRUE.equals(place.getIsDrink()),
                null);
    }
}
