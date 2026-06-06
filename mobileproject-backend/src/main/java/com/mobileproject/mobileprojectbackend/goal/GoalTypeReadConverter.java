package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

/**
 * Converter đọc {@link GoalType} từ MongoDB, xử lý legacy data và giá trị không hợp lệ.
 *
 * <p>MongoDB lưu {@code GoalType} dưới dạng String. Converter này đảm bảo:</p>
 * <ul>
 *   <li>Chuẩn hóa chữ hoa/thường ({@code toUpperCase()})</li>
 *   <li>Fallback về {@code SAVING} nếu giá trị null, rỗng hoặc không xác định</li>
 * </ul>
 */
@ReadingConverter
public enum GoalTypeReadConverter implements Converter<String, GoalType> {
    INSTANCE;

    @Override
    public GoalType convert(String source) {
        if (source == null || source.isBlank()) {
            return GoalType.SAVING; // default for legacy data
        }
        try {
            return GoalType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Unknown value from old documents - map to SAVING as fallback
            // Could also log a warning here
            return GoalType.SAVING;
        }
    }
}
