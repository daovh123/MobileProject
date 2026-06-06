package com.mobileproject.mobileprojectbackend.place;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Service quản lý cache địa điểm sử dụng Spring Cache abstraction.
 *
 * <p>Sử dụng cache name {@code placeById} để lưu trữ đối tượng {@link Place}
 * theo ID. Hỗ trợ lookup đơn, lookup hàng loạt (batch), và xóa cache.</p>
 *
 * <p>Cache này được sử dụng bởi {@link HistoryService} và {@link FavoriteService}
 * để tránh truy vấn MongoDB lặp lại khi hiển thị chi tiết địa điểm.</p>
 */
@Service
public class PlaceCacheService {

    private static final String PLACE_BY_ID_CACHE = "placeById";

    private final PlaceRepository placeRepository;
    private final CacheManager cacheManager;

    public PlaceCacheService(PlaceRepository placeRepository, CacheManager cacheManager) {
        this.placeRepository = placeRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Tìm địa điểm theo ID, ưu tiên đọc từ cache. Nếu cache miss thì query DB và put vào cache.
     *
     * @param placeId ID địa điểm
     * @return Optional chứa địa điểm nếu tìm thấy
     */
    public Optional<Place> findById(String placeId) {
        if (placeId == null || placeId.isBlank()) {
            return Optional.empty();
        }

        Cache cache = cacheManager.getCache(PLACE_BY_ID_CACHE);
        Place cached = getFromCache(cache, placeId);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<Place> loaded = placeRepository.findById(placeId);
        loaded.ifPresent(place -> putToCache(cache, placeId, place));
        return loaded;
    }

    /**
     * Tìm hàng loạt địa điểm theo danh sách ID. Chỉ query DB cho các ID chưa có trong cache.
     *
     * @param placeIds danh sách ID cần tìm
     * @return Map từ placeId → Place
     */
    public Map<String, Place> findAllByIds(List<String> placeIds) {
        if (placeIds == null || placeIds.isEmpty()) {
            return Map.of();
        }

        Cache cache = cacheManager.getCache(PLACE_BY_ID_CACHE);
        Map<String, Place> placeById = new LinkedHashMap<>();
        List<String> missingIds = new ArrayList<>();

        for (String placeId : placeIds) {
            if (placeId == null || placeId.isBlank()) {
                continue;
            }

            Place cached = getFromCache(cache, placeId);
            if (cached != null) {
                placeById.putIfAbsent(placeId, cached);
                continue;
            }

            if (!missingIds.contains(placeId)) {
                missingIds.add(placeId);
            }
        }

        if (!missingIds.isEmpty()) {
            StreamSupport.stream(placeRepository.findAllById(missingIds).spliterator(), false)
                    .forEach(place -> {
                        if (place.getId() == null || place.getId().isBlank()) {
                            return;
                        }
                        placeById.putIfAbsent(place.getId(), place);
                        putToCache(cache, place.getId(), place);
                    });
        }

        return placeById;
    }

    /**
     * Xóa toàn bộ cache địa điểm.
     */
    public void evictAll() {
        Cache cache = cacheManager.getCache(PLACE_BY_ID_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }

    private Place getFromCache(Cache cache, String key) {
        if (cache == null) {
            return null;
        }
        return cache.get(key, Place.class);
    }

    private void putToCache(Cache cache, String key, Place value) {
        if (cache != null) {
            cache.put(key, value);
        }
    }
}
