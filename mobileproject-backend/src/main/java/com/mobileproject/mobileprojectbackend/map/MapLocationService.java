package com.mobileproject.mobileprojectbackend.map;

import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class MapLocationService {

    private final UserLocationRepository userLocationRepository;

    public MapLocationService(UserLocationRepository userLocationRepository) {
        this.userLocationRepository = userLocationRepository;
    }

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
