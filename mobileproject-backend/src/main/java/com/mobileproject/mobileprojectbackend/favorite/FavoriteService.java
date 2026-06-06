package com.mobileproject.mobileprojectbackend.favorite;

import com.mobileproject.mobileprojectbackend.favorite.dto.FavoriteListResponse;
import com.mobileproject.mobileprojectbackend.favorite.dto.FavoriteResponse;
import com.mobileproject.mobileprojectbackend.place.Place;
import com.mobileproject.mobileprojectbackend.place.PlaceCacheService;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Service xử lý yêu thích địa điểm.
 *
 * <p><strong>Business logic:</strong></p>
 * <ul>
 *   <li>Toggle yêu thích: nếu đã yêu thích → xóa, nếu chưa → thêm mới</li>
 *   <li>Lấy danh sách yêu thích: batch load từ cache để tối ưu performance</li>
 *   <li>Kiểm tra trạng thái yêu thích: query theo (userId, placeId)</li>
 * </ul>
 *
 * <p>Sử dụng {@link PlaceCacheService} để load thông tin địa điểm từ cache.</p>
 */
@Service
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final PlaceCacheService placeCacheService; // Đảm bảo field này được sử dụng

    // Sửa Constructor để khớp với các biến field đã khai báo
    public FavoriteService(UserFavoriteRepository favoriteRepository, PlaceCacheService placeCacheService) {
        this.favoriteRepository = favoriteRepository;
        this.placeCacheService = placeCacheService;
    }

    /**
     * Toggle yêu thích: nếu đã tồn tại → xóa, nếu chưa → thêm.
     *
     * @param userId  ID người dùng
     * @param placeId ID địa điểm
     * @return response thông báo trạng thái
     * @throws ResponseStatusException 404 nếu place không tồn tại
     */
    public FavoriteResponse toggleFavorite(String userId, String placeId) {
        // Sử dụng placeCacheService để kiểm tra sự tồn tại của địa điểm
        if (placeCacheService.findById(placeId).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found");
        }

        Optional<UserFavorite> existing = favoriteRepository.findByUserIdAndPlaceId(userId, placeId);
        if (existing.isPresent()) {
            favoriteRepository.delete(existing.get());
            return new FavoriteResponse(true, "Removed from favorites", null, placeId, null);
        }

        UserFavorite saved = favoriteRepository.save(new UserFavorite(userId, placeId));
        return new FavoriteResponse(true, "Added to favorites", saved.getId(), placeId, saved.getCreatedAt());
    }

    /**
     * Lấy danh sách địa điểm yêu thích (mới nhất trước), batch load từ cache.
     */
    public FavoriteListResponse getUserFavorites(String userId) {
        List<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<String> placeIds = favorites.stream().map(UserFavorite::getPlaceId).toList();

        // Lấy dữ liệu hàng loạt từ Cache để tối ưu performance
        Map<String, Place> placeById = placeCacheService.findAllByIds(placeIds);

        List<PlaceDto> places = placeIds.stream()
                .map(placeById::get)
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();

        return new FavoriteListResponse(true, "Favorites retrieved", places);
    }

    /**
     * Kiểm tra user có yêu thích địa điểm hay không.
     */
    public boolean isFavorite(String userId, String placeId) {
        return favoriteRepository.findByUserIdAndPlaceId(userId, placeId).isPresent();
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