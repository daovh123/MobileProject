package com.mobileproject.mobileprojectbackend.favorite;

import com.mobileproject.mobileprojectbackend.favorite.dto.FavoriteListResponse;
import com.mobileproject.mobileprojectbackend.favorite.dto.FavoriteResponse;
import com.mobileproject.mobileprojectbackend.place.Place;
import com.mobileproject.mobileprojectbackend.place.PlaceRepository;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class FavoriteService {

    private final UserFavoriteRepository favoriteRepository;
    private final PlaceRepository placeRepository;

    public FavoriteService(UserFavoriteRepository favoriteRepository, PlaceRepository placeRepository) {
        this.favoriteRepository = favoriteRepository;
        this.placeRepository = placeRepository;
    }

    public FavoriteResponse toggleFavorite(String userId, String placeId) {
        if (!placeRepository.existsById(placeId)) {
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

    public FavoriteListResponse getUserFavorites(String userId) {
        List<UserFavorite> favorites = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<String> placeIds = favorites.stream().map(UserFavorite::getPlaceId).toList();

        Map<String, Place> placeById = StreamSupport.stream(placeRepository.findAllById(placeIds).spliterator(), false)
                .collect(Collectors.toMap(Place::getId, Function.identity(), (first, second) -> first));

        List<PlaceDto> places = placeIds.stream()
                .map(placeById::get)
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();

        return new FavoriteListResponse(true, "Favorites retrieved", places);
    }

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
