package com.mobileproject.mobileprojectbackend.auth;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Catalog tĩnh chứa danh sách các mẫu khung viền avatar có sẵn trong hệ thống.
 *
 * <p>Danh sách khung viền được hardcode, bao gồm:</p>
 * <ul>
 *   <li>Rose (hồng) - #E91E63</li>
 *   <li>Gold (vàng) - #FFC107</li>
 *   <li>Purple (tím) - #9C27B0</li>
 *   <li>Neon (xanh neon) - #00E5FF</li>
 *   <li>Sunset (hoàng hôn) - #FF5722</li>
 *   <li>Ocean (đại dương) - #0288D1</li>
 * </ul>
 */
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

    /** Trả về toàn bộ danh sách khung viền avatar. */
    public List<AvatarFrame> getAll() {
        return FRAMES;
    }

    /**
     * Tìm khung viền theo ID.
     *
     * @param id ID khung viền
     * @return Optional chứa AvatarFrame nếu tìm thấy
     */
    public Optional<AvatarFrame> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return FRAMES.stream().filter(f -> f.id().equals(id)).findFirst();
    }
}
