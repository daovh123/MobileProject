package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFilterOptionsResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceFeatureSummaryResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class PlaceService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("H:mm");
    private static final Pattern OPEN_TIME_PATTERN = Pattern.compile("^(\\d{1,2}:\\d{2})\\s*-\\s*(\\d{1,2}:\\d{2})$");

    private final PlaceRepository placeRepository;

    public PlaceService(PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public PlaceSearchResponse search(PlaceSearchRequest request) {
        SearchCriteria criteria = normalizeRequest(request);
        List<PlaceView> filtered = filterPlaces(criteria);
        List<PlaceView> sorted = sortPlaces(filtered, criteria.sort());
        return toPagedResponse(sorted, criteria.page(), criteria.size());
    }

    public PlaceSearchResponse trending(int page, int size) {
        PlaceSearchRequest request = new PlaceSearchRequest(
                null,
                null,
                null,
                "all",
                null,
                null,
                null,
                null,
                null,
                "trending",
                page,
                size
        );
        return search(request);
    }

    public PlaceDto random(PlaceSearchRequest request) {
        SearchCriteria criteria = normalizeRequest(request);
        List<PlaceView> filtered = filterPlaces(criteria);
        if (filtered.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No place matches selected filters");
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(filtered.size());
        return toDto(filtered.get(randomIndex));
    }

    public PlaceDto findById(String id) {
        Place place = placeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Place not found"));
        boolean openNow = isOpenNow(place, LocalTime.now(VIETNAM_ZONE));
        return toDto(place, openNow, null);
    }

    public PlaceFeatureSummaryResponse getFeatureSummary() {
        List<Place> places = placeRepository.findAll();

        long totalPlaces = places.size();
        long totalFoodPlaces = places.stream().filter(place -> Boolean.TRUE.equals(place.getIsFood())).count();
        long totalDrinkPlaces = places.stream().filter(place -> Boolean.TRUE.equals(place.getIsDrink())).count();
        long totalPinnedPlaces = places.stream().filter(place -> Boolean.TRUE.equals(place.getIsPinned())).count();
        long totalPlacesWithCoordinates = places.stream()
                .filter(place -> place.getLat() != null && place.getLng() != null)
                .count();

        List<PlaceFeatureSummaryResponse.CountItem> topProvinces = topCounts(
                places.stream().map(Place::getProvince).toList(),
                10
        );

        List<PlaceFeatureSummaryResponse.CountItem> topDistricts = topCounts(
                places.stream().flatMap(place -> Stream.of(place.getNormalizedDistrict(), place.getDistrict())).toList(),
                15
        );

        List<String> topTags = places.stream()
                .flatMap(place -> Stream.of(place.getEffectiveTag(), place.getCategory(), place.getMealType()))
                .map(this::normalizeNullable)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(20)
                .map(Map.Entry::getKey)
                .toList();

        return new PlaceFeatureSummaryResponse(
                totalPlaces,
                totalFoodPlaces,
                totalDrinkPlaces,
                totalPinnedPlaces,
                totalPlacesWithCoordinates,
                topProvinces,
                topDistricts,
                topTags
        );
    }

        public PlaceFilterOptionsResponse getFilterOptions() {
        List<Place> places = placeRepository.findAll();

        List<String> districts = collectDistinctValues(
            places.stream().flatMap(place -> Stream.of(place.getNormalizedDistrict(), place.getDistrict())).toList(),
            300
        );
        List<String> provinces = collectDistinctValues(
            places.stream().map(Place::getProvince).toList(),
            120
        );

        return new PlaceFilterOptionsResponse(districts, provinces);
        }

    private List<PlaceView> filterPlaces(SearchCriteria criteria) {
        LocalTime now = LocalTime.now(VIETNAM_ZONE);

        return placeRepository.findAll().stream()
                .map(place -> toPlaceView(place, criteria.nearLat(), criteria.nearLng(), now))
                .filter(view -> matchesQuery(view.place(), criteria.query()))
                .filter(view -> matchesProvince(view.place(), criteria.province()))
                .filter(view -> matchesDistrict(view.place(), criteria.district()))
                .filter(view -> matchesType(view.place(), criteria.type()))
                .filter(view -> matchesMinRating(view.place(), criteria.minRating()))
                .filter(view -> matchesOpenNow(view, criteria.openNow()))
                .filter(view -> matchesRadius(view, criteria.radiusKm()))
                .toList();
    }

    private List<PlaceView> sortPlaces(List<PlaceView> places, String sort) {
        Comparator<PlaceView> comparator = switch (sort) {
            case "rating" -> ratingComparator();
            case "distance" -> distanceComparator();
            default -> trendingComparator();
        };

        return places.stream().sorted(comparator).toList();
    }

    private PlaceSearchResponse toPagedResponse(List<PlaceView> sortedPlaces, int page, int size) {
        long total = sortedPlaces.size();
        long fromIndexLong = (long) page * size;

        if (fromIndexLong >= total) {
            return new PlaceSearchResponse(List.of(), total, page, size);
        }

        int fromIndex = (int) fromIndexLong;
        int toIndex = (int) Math.min(fromIndexLong + size, total);

        List<PlaceDto> items = sortedPlaces.subList(fromIndex, toIndex)
                .stream()
                .map(this::toDto)
                .toList();

        return new PlaceSearchResponse(items, total, page, size);
    }

    private SearchCriteria normalizeRequest(PlaceSearchRequest request) {
        int safePage = Math.max(request.page(), 0);
        int safeSize = request.size() <= 0 ? DEFAULT_PAGE_SIZE : Math.min(request.size(), MAX_PAGE_SIZE);

        String type = normalizeFilterValue(request.type(), "all");
        if (!Set.of("all", "food", "drink").contains(type)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid type value. Use food, drink, or all");
        }

        String sort = normalizeFilterValue(request.sort(), "trending");
        if (!Set.of("trending", "rating", "distance").contains(sort)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid sort value. Use trending, rating, or distance");
        }

        Double minRating = request.minRating();
        if (minRating != null && (minRating < 0 || minRating > 5)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minRating must be between 0 and 5");
        }

        Double nearLat = request.nearLat();
        Double nearLng = request.nearLng();
        boolean hasGeo = nearLat != null && nearLng != null;

        if ((nearLat == null) != (nearLng == null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nearLat and nearLng must be provided together");
        }

        Double radiusKm = request.radiusKm();
        if (radiusKm != null) {
            if (!hasGeo) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "radiusKm requires nearLat and nearLng");
            }
            if (radiusKm <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "radiusKm must be greater than 0");
            }
        }

        if ("distance".equals(sort) && !hasGeo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "distance sort requires nearLat and nearLng");
        }

        return new SearchCriteria(
                normalizeNullable(request.q()),
                normalizeNullable(request.province()),
                normalizeNullable(request.district()),
                type,
                minRating,
                Boolean.TRUE.equals(request.openNow()),
                nearLat,
                nearLng,
                radiusKm,
                sort,
                safePage,
                safeSize
        );
    }

    private PlaceView toPlaceView(Place place, Double nearLat, Double nearLng, LocalTime now) {
        Double distanceKm = calculateDistanceKm(nearLat, nearLng, place);
        boolean openNow = isOpenNow(place, now);
        return new PlaceView(place, distanceKm, openNow);
    }

    private PlaceDto toDto(PlaceView view) {
        return toDto(view.place(), view.openNow(), view.distanceKm());
    }

    private PlaceDto toDto(Place place, boolean openNow, Double distanceKm) {
        return new PlaceDto(
                place.getId(),
                place.getName(),
                place.getAddress(),
                place.getDistrict(),
                place.getCategory(),
                place.getMealType(),
                place.getRating(),
                place.getReviewCount(),
                place.getOpeningHours(),
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
                place.getOpenTime(),
                openNow,
                roundDistance(distanceKm)
        );
    }

    private List<PlaceFeatureSummaryResponse.CountItem> topCounts(List<String> values, int limit) {
        return values.stream()
                .map(this::normalizeNullable)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry::getKey))
                .limit(limit)
                .map(entry -> new PlaceFeatureSummaryResponse.CountItem(entry.getKey(), entry.getValue()))
                .toList();
    }

            private List<String> collectDistinctValues(List<String> values, int limit) {
            Map<String, String> normalizedValueMap = new LinkedHashMap<>();

            values.stream()
                .map(this::trimToNull)
                .filter(Objects::nonNull)
                .forEach(value -> normalizedValueMap.putIfAbsent(value.toLowerCase(Locale.ROOT), value));

            return normalizedValueMap.values().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .limit(limit)
                .toList();
            }

    private Comparator<PlaceView> trendingComparator() {
        return Comparator
                .comparing((PlaceView view) -> Boolean.TRUE.equals(view.place().getIsPinned())).reversed()
                .thenComparing((PlaceView view) -> defaultDouble(view.place().getRating()), Comparator.reverseOrder())
                .thenComparing((PlaceView view) -> defaultInt(view.place().getReviewCount()), Comparator.reverseOrder())
                .thenComparing(view -> normalizeFilterValue(view.place().getName(), ""));
    }

    private Comparator<PlaceView> ratingComparator() {
        return Comparator
                .comparing((PlaceView view) -> defaultDouble(view.place().getRating()), Comparator.reverseOrder())
                .thenComparing((PlaceView view) -> defaultInt(view.place().getReviewCount()), Comparator.reverseOrder())
                .thenComparing((PlaceView view) -> Boolean.TRUE.equals(view.place().getIsPinned())).reversed()
                .thenComparing(view -> normalizeFilterValue(view.place().getName(), ""));
    }

    private Comparator<PlaceView> distanceComparator() {
        return Comparator
                .comparing((PlaceView view) -> view.distanceKm() == null ? Double.MAX_VALUE : view.distanceKm())
                .thenComparing(trendingComparator());
    }

    private boolean matchesQuery(Place place, String query) {
        if (query == null) {
            return true;
        }

        return containsLowerCase(place.getSearchString(), query)
                || containsLowerCase(place.getName(), query)
                || containsLowerCase(place.getAddress(), query)
                || containsLowerCase(place.getEffectiveTag(), query);
    }

    private boolean matchesProvince(Place place, String province) {
        if (province == null) {
            return true;
        }
        return containsLowerCase(place.getProvince(), province);
    }

    private boolean matchesDistrict(Place place, String district) {
        if (district == null) {
            return true;
        }
        return containsLowerCase(place.getDistrict(), district)
                || containsLowerCase(place.getNormalizedDistrict(), district);
    }

    private boolean matchesType(Place place, String type) {
        return switch (type) {
            case "food" -> Boolean.TRUE.equals(place.getIsFood());
            case "drink" -> Boolean.TRUE.equals(place.getIsDrink());
            default -> true;
        };
    }

    private boolean matchesMinRating(Place place, Double minRating) {
        if (minRating == null) {
            return true;
        }
        Double rating = place.getRating();
        return rating != null && rating >= minRating;
    }

    private boolean matchesOpenNow(PlaceView view, boolean openNowFilter) {
        return !openNowFilter || view.openNow();
    }

    private boolean matchesRadius(PlaceView view, Double radiusKm) {
        if (radiusKm == null) {
            return true;
        }
        return view.distanceKm() != null && view.distanceKm() <= radiusKm;
    }

    private boolean containsLowerCase(String source, String expectedLowerCase) {
        return source != null && source.toLowerCase(Locale.ROOT).contains(expectedLowerCase);
    }

    private boolean isOpenNow(Place place, LocalTime now) {
        String rawRange = firstNotBlank(place.getOpenTime(), place.getOpeningHours());
        if (rawRange == null) {
            return false;
        }

        Matcher matcher = OPEN_TIME_PATTERN.matcher(rawRange.trim());
        if (!matcher.matches()) {
            return false;
        }

        try {
            LocalTime start = LocalTime.parse(matcher.group(1), TIME_FORMATTER);
            LocalTime end = LocalTime.parse(matcher.group(2), TIME_FORMATTER);

            if (start.equals(end)) {
                return true;
            }

            if (end.isAfter(start)) {
                return !now.isBefore(start) && now.isBefore(end);
            }

            return !now.isBefore(start) || now.isBefore(end);
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    private Double calculateDistanceKm(Double nearLat, Double nearLng, Place place) {
        if (nearLat == null || nearLng == null || place.getLat() == null || place.getLng() == null) {
            return null;
        }

        double lat1 = Math.toRadians(nearLat);
        double lon1 = Math.toRadians(nearLng);
        double lat2 = Math.toRadians(place.getLat());
        double lon2 = Math.toRadians(place.getLng());

        double deltaLat = lat2 - lat1;
        double deltaLon = lon2 - lon1;

        double sinLat = Math.sin(deltaLat / 2);
        double sinLon = Math.sin(deltaLon / 2);

        double a = sinLat * sinLat + Math.cos(lat1) * Math.cos(lat2) * sinLon * sinLon;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return 6371.0 * c;
    }

    private Double roundDistance(Double distanceKm) {
        if (distanceKm == null) {
            return null;
        }
        return Math.round(distanceKm * 1000.0) / 1000.0;
    }

    private double defaultDouble(Double value) {
        return value == null ? 0.0 : value;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            return null;
        }
        return normalized.toLowerCase(Locale.ROOT);
    }

    private String normalizeFilterValue(String value, String fallback) {
        String normalized = normalizeNullable(value);
        return normalized == null ? fallback : normalized;
    }

    private String firstNotBlank(String first, String second) {
        String normalizedFirst = normalizeNullable(first);
        if (normalizedFirst != null) {
            return normalizedFirst;
        }
        return normalizeNullable(second);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isBlank() ? null : trimmed;
    }

    private record SearchCriteria(
            String query,
            String province,
            String district,
            String type,
            Double minRating,
            boolean openNow,
            Double nearLat,
            Double nearLng,
            Double radiusKm,
            String sort,
            int page,
            int size
    ) {
    }

    private record PlaceView(Place place, Double distanceKm, boolean openNow) {
    }
}