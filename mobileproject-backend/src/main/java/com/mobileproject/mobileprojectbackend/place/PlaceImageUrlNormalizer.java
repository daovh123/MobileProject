package com.mobileproject.mobileprojectbackend.place;

import java.net.URI;
import java.util.Locale;
import java.util.Set;

final class PlaceImageUrlNormalizer {

    private static final Set<String> INVALID_LITERALS = Set.of(
            "n/a",
            "na",
            "none",
            "null",
            "-",
            "_",
            "n.a");

    private PlaceImageUrlNormalizer() {
    }

    static String normalize(String rawValue) {
        if (rawValue == null) {
            return null;
        }

        String candidate = rawValue.trim();
        if (candidate.isBlank()) {
            return null;
        }

        String lower = candidate.toLowerCase(Locale.ROOT);
        if (INVALID_LITERALS.contains(lower)) {
            return null;
        }

        if (candidate.startsWith("//")) {
            candidate = "https:" + candidate;
            lower = candidate.toLowerCase(Locale.ROOT);
        } else if (lower.startsWith("www.")) {
            candidate = "https://" + candidate;
            lower = candidate.toLowerCase(Locale.ROOT);
        } else if (!(lower.startsWith("http://") || lower.startsWith("https://"))) {
            return null;
        }

        URI uri;
        try {
            uri = URI.create(candidate);
        } catch (IllegalArgumentException illegalArgumentException) {
            return null;
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            return null;
        }

        String hostLower = host.toLowerCase(Locale.ROOT);
        String pathLower = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);

        if ("maps.app.goo.gl".equals(hostLower)) {
            return null;
        }

        if (("goo.gl".equals(hostLower) || hostLower.endsWith(".goo.gl")) && pathLower.startsWith("/maps")) {
            return null;
        }

        if (("google.com".equals(hostLower) || "www.google.com".equals(hostLower)
                || "maps.google.com".equals(hostLower))
                && pathLower.startsWith("/maps")) {
            return null;
        }

        return candidate;
    }
}
