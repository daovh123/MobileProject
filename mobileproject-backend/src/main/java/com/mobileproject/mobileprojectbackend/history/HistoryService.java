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

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final UserHistoryRepository historyRepository;
    private final PlaceCacheService placeCacheService;

    public Map<String, Object> recordView(String userId, String placeId) {
        if (placeCacheService.findById(placeId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }

        historyRepository.save(new UserHistory(userId, placeId));
        return Map.of("success", true, "message", "View recorded");
    }

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
