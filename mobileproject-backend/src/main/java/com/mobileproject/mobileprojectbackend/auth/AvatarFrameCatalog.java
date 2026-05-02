package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AvatarFrameCatalog {

    private static final List<AvatarFrame> FRAMES = List.of(
            new AvatarFrame("frame_rose", "Rose", "rose", "#E91E63"),
            new AvatarFrame("frame_gold", "Gold", "gold", "#FFC107"),
            new AvatarFrame("frame_purple", "Purple", "purple", "#9C27B0"),
            new AvatarFrame("frame_neon", "Neon", "neon", "#00E5FF"),
            new AvatarFrame("frame_gradient_sunset", "Sunset", "gradient_sunset", "#FF5722"),
            new AvatarFrame("frame_gradient_ocean", "Ocean", "gradient_ocean", "#0288D1")
    );

    public List<AvatarFrame> getAll() {
        return FRAMES;
    }

    public Optional<AvatarFrame> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return FRAMES.stream().filter(f -> f.id().equals(id)).findFirst();
    }
}
