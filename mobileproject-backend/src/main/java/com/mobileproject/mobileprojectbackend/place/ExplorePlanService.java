package com.mobileproject.mobileprojectbackend.place;

import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanItemResponse;
import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanRequest;
import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanResponse;
import com.mobileproject.mobileprojectbackend.place.dto.ExplorePlanSummaryResponse;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceDto;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchRequest;
import com.mobileproject.mobileprojectbackend.place.dto.PlaceSearchResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExplorePlanService {

    private static final long DEFAULT_SUGGESTED_BUDGET = 30_000L;
    private static final long LOW_BALANCE_THRESHOLD = 30_000L;
    private static final int SEARCH_POOL_SIZE = 200;
    private static final int PLAN_VARIANT_SHORTLIST_SIZE = 5;
    private static final Set<String> GENERIC_BRAND_TOKENS = Set.of(
            "an", "banh", "bar", "bo", "bun", "cafe", "coffee", "do", "drink", "food", "hang",
            "mon", "nha", "nuoc", "pho", "quan", "restaurant", "tea", "thuc", "tiem", "tra", "uong");
    private static final Pattern PRICE_TOKEN_PATTERN = Pattern.compile("(\\d+(?:[\\.,]\\d+)?)\\s*(tr|m|k|ngh[iì]n|tri[eệ]u)?");
    private static final Pattern PERSON_COUNT_PATTERN = Pattern.compile("(\\d+)\\s*(?:nguoi|ng)");
    private static final Pattern NON_NUMERIC_RANGE_SEPARATOR = Pattern.compile("\\s*(?:-|~|to|den|t[ớo]i)\\s*");

    private final PlaceService placeService;

    public ExplorePlanService(PlaceService placeService) {
        this.placeService = placeService;
    }

    public ExplorePlanResponse buildPlan(ExplorePlanRequest request) {
        int peopleCount = request.peopleCount() == null ? 2 : request.peopleCount();
        int desiredStops = request.desiredStops() == null ? 2 : request.desiredStops();
        long totalBudget = request.budget();
        long randomSeed = request.randomSeed() == null ? System.currentTimeMillis() : request.randomSeed();
        Set<String> excludedPlaceIds = normalizeTokenSet(request.excludePlaceIds());
        excludedPlaceIds.addAll(normalizeTokenSet(request.viewedPlaceIds()));
        excludedPlaceIds.addAll(normalizeTokenSet(request.gonePlaceIds()));
        excludedPlaceIds.addAll(normalizeTokenSet(request.sentPlaceIds()));
        Set<String> recentKeywords = normalizeKeywordSet(request.recentKeywords());
        Set<String> recentPhrases = normalizePhraseSet(request.recentKeywords());
        if (totalBudget < 1_000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "budget must be at least 1000");
        }

        PlaceSearchResponse searchResponse = placeService.search(new PlaceSearchRequest(
                request.q(),
                request.province(),
                request.district(),
                normalizeType(request.type()),
                request.minRating(),
                request.nearLat(),
                request.nearLng(),
                request.radiusKm(),
                request.nearLat() != null && request.nearLng() != null ? "distance" : "ratingmix",
                0,
                SEARCH_POOL_SIZE));

        List<PlaceDto> candidates = searchResponse.items();
        if (candidates.isEmpty()) {
            return new ExplorePlanResponse(
                    buildSummary(totalBudget, peopleCount, desiredStops, 0L),
                    List.of());
        }

        List<ExplorePlanItemResponse> items = selectPlanItems(
                candidates,
                totalBudget,
                desiredStops,
                excludedPlaceIds,
                recentKeywords,
                recentPhrases,
                randomSeed);
        long estimatedTotalCost = items.stream()
                .map(ExplorePlanItemResponse::estimatedCost)
                .filter(Objects::nonNull)
                .mapToLong(Long::longValue)
                .sum();

        return new ExplorePlanResponse(
                buildSummary(totalBudget, peopleCount, desiredStops, estimatedTotalCost),
                items);
    }

    private ExplorePlanSummaryResponse buildSummary(long totalBudget, int peopleCount, int desiredStops,
                                                    long estimatedTotalCost) {
        boolean lowBalance = totalBudget < LOW_BALANCE_THRESHOLD;
        return new ExplorePlanSummaryResponse(
                totalBudget,
                peopleCount,
                desiredStops,
                estimatedTotalCost,
                lowBalance,
                lowBalance ? "Quy chung cua 2 ban con rat it, vui long nap them." : null,
                DEFAULT_SUGGESTED_BUDGET);
    }

    private List<ExplorePlanItemResponse> selectPlanItems(List<PlaceDto> candidates,
                                                          long totalBudget,
                                                          int desiredStops,
                                                          Set<String> excludedPlaceIds,
                                                          Set<String> recentKeywords,
                                                          Set<String> recentPhrases,
                                                          long randomSeed) {
        List<PlaceCandidate> pool = candidates.stream()
                .map(place -> new PlaceCandidate(
                        place,
                        estimateCost(place),
                        resolveExperienceType(place),
                        normalizeValue(place.name()),
                        placeKeywords(place),
                        cuisineKeywords(place),
                        brandKeywords(place),
                        canonicalValues(place)))
                .sorted(Comparator
                        .comparingLong((PlaceCandidate candidate) -> candidate.estimatedCost == null ? Long.MAX_VALUE : candidate.estimatedCost)
                        .thenComparing((PlaceCandidate candidate) -> candidate.place.rating(), Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing((PlaceCandidate candidate) -> candidate.place.reviewCount(), Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<ExplorePlanItemResponse> selected = new ArrayList<>();
        Set<String> usedPlaceIds = new HashSet<>();
        Set<String> usedCuisineKeywords = new HashSet<>();
        Set<String> usedBrandKeywords = new HashSet<>();
        long remainingBudget = totalBudget;
        String previousExperienceType = null;

        for (int stopIndex = 0; stopIndex < desiredStops; stopIndex++) {
            int remainingStops = desiredStops - stopIndex;
            long targetCost = Math.max(remainingBudget / Math.max(remainingStops, 1), 1L);
            String preferredExperienceType = preferredExperienceType(stopIndex, previousExperienceType);

            PlaceCandidate chosen = chooseCandidate(
                    pool,
                    usedPlaceIds,
                    excludedPlaceIds,
                    recentKeywords,
                    recentPhrases,
                    usedCuisineKeywords,
                    usedBrandKeywords,
                    preferredExperienceType,
                    targetCost,
                    remainingBudget,
                    randomSeed,
                    stopIndex);
            if (chosen == null) {
                break;
            }

            usedPlaceIds.add(chosen.place.id());
            usedCuisineKeywords.addAll(chosen.cuisineKeywords);
            usedBrandKeywords.addAll(chosen.brandKeywords);
            long estimatedCost = chosen.estimatedCost == null ? targetCost : chosen.estimatedCost;
            remainingBudget = Math.max(remainingBudget - estimatedCost, 0L);
            previousExperienceType = chosen.experienceType;

            selected.add(new ExplorePlanItemResponse(
                    stopIndex + 1,
                    chosen.experienceType,
                    estimatedCost,
                    buildReason(chosen, targetCost, preferredExperienceType),
                    chosen.place));
        }

        return selected;
    }

    private PlaceCandidate chooseCandidate(List<PlaceCandidate> pool,
                                           Set<String> usedPlaceIds,
                                           Set<String> excludedPlaceIds,
                                           Set<String> recentKeywords,
                                           Set<String> recentPhrases,
                                           Set<String> usedCuisineKeywords,
                                           Set<String> usedBrandKeywords,
                                           String preferredExperienceType,
                                           long targetCost,
                                           long remainingBudget,
                                           long randomSeed,
                                           int stopIndex) {
        List<PlaceCandidate> unusedCandidates = pool.stream()
                .filter(candidate -> !usedPlaceIds.contains(candidate.place.id()))
                .filter(candidate -> !excludedPlaceIds.contains(normalizeToken(candidate.place.id())))
                .toList();
        if (unusedCandidates.isEmpty()) {
            return null;
        }

        List<PlaceCandidate> budgetSafeCandidates = unusedCandidates.stream()
                .filter(candidate -> candidate.estimatedCost != null && candidate.estimatedCost <= remainingBudget)
                .toList();
        if (budgetSafeCandidates.isEmpty()) {
            return null;
        }

        Comparator<PlaceCandidate> ranking = Comparator
                .<PlaceCandidate>comparingInt(candidate -> exactRecentMatch(candidate, recentPhrases) ? 1 : 0)
                .thenComparingInt((PlaceCandidate candidate) -> overlapCount(candidate.cuisineKeywords, recentKeywords))
                .thenComparingInt((PlaceCandidate candidate) -> overlapCount(candidate.brandKeywords, recentKeywords))
                .thenComparingInt((PlaceCandidate candidate) -> overlapCount(candidate.keywords, recentKeywords))
                .thenComparingInt((PlaceCandidate candidate) -> preferredExperienceType.equals(candidate.experienceType) ? 0 : 1)
                .thenComparingInt((PlaceCandidate candidate) -> overlapCount(candidate.cuisineKeywords, usedCuisineKeywords))
                .thenComparingInt((PlaceCandidate candidate) -> overlapCount(candidate.brandKeywords, usedBrandKeywords))
                .thenComparingLong((PlaceCandidate candidate) -> Math.abs((candidate.estimatedCost == null ? targetCost : candidate.estimatedCost) - targetCost))
                .thenComparing((PlaceCandidate candidate) -> candidate.place.rating(), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing((PlaceCandidate candidate) -> candidate.place.reviewCount(), Comparator.nullsLast(Comparator.reverseOrder()));

        List<PlaceCandidate> rankedCandidates = budgetSafeCandidates.stream()
                .sorted(ranking)
                .toList();
        if (rankedCandidates.isEmpty()) {
            return null;
        }

        int shortlistSize = Math.min(PLAN_VARIANT_SHORTLIST_SIZE, rankedCandidates.size());
        int pickIndex = Math.floorMod(
                Objects.hash(randomSeed, stopIndex, preferredExperienceType, targetCost, remainingBudget, usedPlaceIds.size()),
                shortlistSize);
        return rankedCandidates.get(pickIndex);
    }

    private String buildReason(PlaceCandidate candidate, long targetCost, String preferredExperienceType) {
        List<String> reasons = new ArrayList<>();
        if (preferredExperienceType.equals(candidate.experienceType)) {
            reasons.add("Da dang hoa lich trinh");
        }
        if (candidate.estimatedCost != null && candidate.estimatedCost <= targetCost * 12 / 10) {
            reasons.add("Hop budget");
        }
        if (candidate.place.rating() != null && candidate.place.rating() >= 4.3) {
            reasons.add("Danh gia cao");
        }
        if (reasons.isEmpty()) {
            reasons.add("Phu hop voi bo loc hien tai");
        }
        return String.join(" • ", reasons);
    }

    private boolean exactRecentMatch(PlaceCandidate candidate, Set<String> recentPhrases) {
        if (recentPhrases.isEmpty()) {
            return false;
        }
        if (candidate.normalizedName != null && recentPhrases.contains(candidate.normalizedName)) {
            return true;
        }
        for (String canonicalValue : candidate.canonicalValues) {
            if (recentPhrases.contains(canonicalValue)) {
                return true;
            }
        }
        return false;
    }

    private int overlapCount(Set<String> values, Set<String> candidates) {
        if (values.isEmpty() || candidates.isEmpty()) {
            return 0;
        }
        int overlapCount = 0;
        for (String value : values) {
            if (candidates.contains(value)) {
                overlapCount++;
            }
        }
        return overlapCount;
    }

    private String preferredExperienceType(int stopIndex, String previousExperienceType) {
        if (stopIndex == 0) {
            return "food";
        }
        if ("food".equals(previousExperienceType)) {
            return "drink";
        }
        return "food";
    }

    private Long estimateCost(PlaceDto place) {
        String priceRange = place.priceRange();
        if (priceRange != null && !priceRange.isBlank()) {
            List<Long> values = parsePriceCandidates(priceRange);
            if (!values.isEmpty()) {
                long sum = 0L;
                for (Long value : values) {
                    sum += value;
                }
                return sum / values.size();
            }
        }

        if (Boolean.TRUE.equals(place.food())) {
            return 80_000L;
        }
        if (Boolean.TRUE.equals(place.drink())) {
            return 35_000L;
        }

        String foldedText = foldText(place.effectiveTag()) + " " + foldText(place.category()) + " " + foldText(place.mealType());
        if (foldedText.contains("coffee") || foldedText.contains("ca phe") || foldedText.contains("tra sua")) {
            return 35_000L;
        }
        return 60_000L;
    }

    private List<Long> parsePriceCandidates(String priceRange) {
        String normalized = foldText(priceRange)
                .replace("vnd", "")
                .replace("vnđ", "")
                .replace("dong", "")
                .replace("đ", "")
                .replace("/phan", "")
                .replace("gia tu", "")
                .replace("chi tu", "")
                .replace("khoang", "")
                .replace("tam", "")
                .replace("xap xi", "")
                .replace("tu ", "")
                .replace("tren ", "")
                .replace("duoi ", "")
                .replace("hon ", "")
                .trim();

        boolean perPerson = normalized.contains("/nguoi") || normalized.contains("mot nguoi");
        Integer personCount = extractPersonCount(normalized);

        String sanitized = normalized
                .replace("/nguoi", "")
                .replace("mot nguoi", "")
                .replace("1 nguoi", "")
                .replaceAll("\\b\\d+\\s*(?:nguoi|ng)\\b", "")
                .replaceAll("\\s+", " ")
                .trim();

        String[] segments = NON_NUMERIC_RANGE_SEPARATOR.split(sanitized);
        List<Long> values = new ArrayList<>();
        for (String segment : segments) {
            Matcher matcher = PRICE_TOKEN_PATTERN.matcher(segment);
            while (matcher.find()) {
                Long parsed = parsePriceToken(matcher.group(1), matcher.group(2));
                if (parsed != null) {
                    values.add(parsed);
                }
            }
        }
        if (values.size() == 1 && !perPerson && personCount != null && personCount > 1) {
            long normalizedPerPerson = Math.max(Math.round((double) values.getFirst() / personCount), 1L);
            return List.of(normalizedPerPerson);
        }
        return values;
    }

    private Long parsePriceToken(String rawNumber, String rawUnit) {
        if (rawNumber == null || rawNumber.isBlank()) {
            return null;
        }

        String normalizedUnit = rawUnit == null ? "" : rawUnit.trim().toLowerCase(Locale.ROOT);
        String compact = rawNumber.trim().replace(" ", "");
        double value;
        try {
            if (normalizedUnit.isBlank()) {
                value = parseUnitlessPrice(compact);
            } else {
                String decimalReady = normalizeDecimalNumber(compact);
                value = Double.parseDouble(decimalReady);
                if ("k".equals(normalizedUnit) || normalizedUnit.startsWith("ngh")) {
                    value *= 1_000d;
                } else if ("m".equals(normalizedUnit) || normalizedUnit.startsWith("tr")) {
                    value *= 1_000_000d;
                }
            }
        } catch (NumberFormatException exception) {
            return null;
        }

        if (value <= 0d) {
            return null;
        }
        return Math.round(value);
    }

    private double parseUnitlessPrice(String compact) {
        if (compact.matches("\\d{1,3}(?:[\\.,]\\d{3})+")) {
            return Double.parseDouble(compact.replace(",", "").replace(".", ""));
        }
        String digitsOnly = compact.replace(",", "").replace(".", "");
        if (digitsOnly.isBlank()) {
            throw new NumberFormatException("No digits");
        }
        double value = Double.parseDouble(digitsOnly);
        if (value < 1_000d) {
            value *= 1_000d;
        }
        return value;
    }

    private String normalizeDecimalNumber(String rawNumber) {
        if (rawNumber.matches("\\d{1,3}(?:[\\.,]\\d{3})+")) {
            return rawNumber.replace(",", "").replace(".", "");
        }
        return rawNumber.replace(",", ".");
    }

    private Integer extractPersonCount(String normalizedPriceRange) {
        Matcher matcher = PERSON_COUNT_PATTERN.matcher(normalizedPriceRange);
        if (!matcher.find()) {
            return null;
        }
        try {
            return Integer.parseInt(matcher.group(1));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String resolveExperienceType(PlaceDto place) {
        if (Boolean.TRUE.equals(place.food()) && !Boolean.TRUE.equals(place.drink())) {
            return "food";
        }
        if (Boolean.TRUE.equals(place.drink()) && !Boolean.TRUE.equals(place.food())) {
            return "drink";
        }

        String foldedText = foldText(place.effectiveTag()) + " " + foldText(place.category()) + " " + foldText(place.mealType());
        if (foldedText.contains("coffee") || foldedText.contains("ca phe") || foldedText.contains("tra sua")) {
            return "drink";
        }
        return "food";
    }

    private String foldText(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT);
    }

    private Set<String> normalizeTokenSet(List<String> rawValues) {
        Set<String> result = new HashSet<>();
        if (rawValues == null) {
            return result;
        }
        for (String rawValue : rawValues) {
            String normalized = normalizeToken(rawValue);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private Set<String> normalizeKeywordSet(List<String> rawValues) {
        Set<String> result = new LinkedHashSet<>();
        if (rawValues == null) {
            return result;
        }
        for (String rawValue : rawValues) {
            String folded = foldText(rawValue);
            if (folded.isBlank()) {
                continue;
            }
            for (String token : folded.split("\\s+")) {
                String normalized = normalizeToken(token);
                if (normalized != null && normalized.length() >= 3) {
                    result.add(normalized);
                }
            }
        }
        return result;
    }

    private Set<String> normalizePhraseSet(List<String> rawValues) {
        Set<String> result = new LinkedHashSet<>();
        if (rawValues == null) {
            return result;
        }
        for (String rawValue : rawValues) {
            String normalized = normalizeValue(rawValue);
            if (normalized != null) {
                result.add(normalized);
            }
        }
        return result;
    }

    private Set<String> placeKeywords(PlaceDto place) {
        Set<String> result = new LinkedHashSet<>();
        collectKeywords(result, place.name());
        collectKeywords(result, place.effectiveTag());
        collectKeywords(result, place.category());
        collectKeywords(result, place.mealType());
        return result;
    }

    private Set<String> cuisineKeywords(PlaceDto place) {
        Set<String> result = new LinkedHashSet<>();
        collectKeywords(result, place.effectiveTag());
        collectKeywords(result, place.category());
        collectKeywords(result, place.mealType());
        return result;
    }

    private Set<String> brandKeywords(PlaceDto place) {
        Set<String> result = new LinkedHashSet<>();
        String foldedName = foldText(place.name());
        if (foldedName.isBlank()) {
            return result;
        }
        for (String token : foldedName.split("\\s+")) {
            String normalized = normalizeToken(token);
            if (normalized != null && normalized.length() >= 3 && !GENERIC_BRAND_TOKENS.contains(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private Set<String> canonicalValues(PlaceDto place) {
        Set<String> values = new LinkedHashSet<>();
        addCanonicalValue(values, place.name());
        addCanonicalValue(values, place.effectiveTag());
        addCanonicalValue(values, place.category());
        addCanonicalValue(values, place.mealType());
        return values;
    }

    private void collectKeywords(Set<String> target, String rawValue) {
        String folded = foldText(rawValue);
        if (folded.isBlank()) {
            return;
        }
        for (String token : folded.split("\\s+")) {
            String normalized = normalizeToken(token);
            if (normalized != null && normalized.length() >= 3) {
                target.add(normalized);
            }
        }
    }

    private String normalizeToken(String rawValue) {
        if (rawValue == null) {
            return null;
        }
        String normalized = rawValue.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

    private String normalizeValue(String rawValue) {
        String folded = foldText(rawValue);
        if (folded.isBlank()) {
            return null;
        }
        return folded.replaceAll("\\s+", " ").trim();
    }

    private void addCanonicalValue(Set<String> target, String rawValue) {
        String normalized = normalizeValue(rawValue);
        if (normalized != null) {
            target.add(normalized);
        }
    }

    private String normalizeType(String type) {
        if (type == null || type.isBlank()) {
            return "all";
        }
        return type.trim().toLowerCase(Locale.ROOT);
    }

    private record PlaceCandidate(PlaceDto place,
                                  Long estimatedCost,
                                  String experienceType,
                                  String normalizedName,
                                  Set<String> keywords,
                                  Set<String> cuisineKeywords,
                                  Set<String> brandKeywords,
                                  Set<String> canonicalValues) {
    }
}
