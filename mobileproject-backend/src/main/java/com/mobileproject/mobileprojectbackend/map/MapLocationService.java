package com.mobileproject.mobileprojectbackend.map;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Service xử lý nghiệp vụ chia sẻ vị trí giữa cặp đôi.
 *
 * <p><strong>Business logic:</strong></p>
 * <ul>
 *   <li>Upsert vị trí: tìm theo (coupleId, userId), nếu có thì cập nhật, nếu không thì tạo mới</li>
 *   <li>ID format: {@code coupleId:userId} để đảm bảo uniqueness</li>
 *   <li>Validate tọa độ: latitude [-90, 90], longitude [-180, 180]</li>
 * </ul>
 */
@Service
public class MapLocationService {

    private final UserLocationRepository userLocationRepository;

    public MapLocationService(UserLocationRepository userLocationRepository) {
        this.userLocationRepository = userLocationRepository;
    }

    /**
     * Cập nhật hoặc tạo mới vị trí user.
     *
     * @param coupleId  ID couple
     * @param userId    ID user
     * @param latitude  vĩ độ (-90 đến 90)
     * @param longitude kinh độ (-180 đến 180)
     * @param updatedAt thời điểm cập nhật (ISO-8601, null → dùng thời gian hiện tại)
     * @return entity vị trí đã lưu
     * @throws IllegalArgumentException nếu tọa độ không hợp lệ
     */
    public UserLocation upsertUserLocation(
            String coupleId,
            String userId,
            double latitude,
            double longitude,
            String updatedAt
    ) {
        validateCoordinates(latitude, longitude);

        String effectiveUpdatedAt = (updatedAt == null || updatedAt.isBlank())
                ? Instant.now().toString()
                : updatedAt;

        UserLocation location = userLocationRepository
                .findByCoupleIdAndUserId(coupleId, userId)
                .orElseGet(UserLocation::new);

        location.setId(buildId(coupleId, userId));
        location.setCoupleId(coupleId);
        location.setUserId(userId);
        location.setLocation(new GeoJsonPoint(longitude, latitude));
        location.setUpdatedAt(effectiveUpdatedAt);

        return userLocationRepository.save(location);
    }

    /**
     * Tìm vị trí hiện tại của user trong couple.
     *
     * @return entity vị trí, hoặc null nếu chưa có
     */
    public UserLocation findUserLocation(String coupleId, String userId) {
        return userLocationRepository.findByCoupleIdAndUserId(coupleId, userId).orElse(null);
    }

    private String buildId(String coupleId, String userId) {
        return coupleId + ":" + userId;
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (Double.isNaN(latitude) || Double.isInfinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude");
        }
        if (Double.isNaN(longitude) || Double.isInfinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude");
        }
    }
}
