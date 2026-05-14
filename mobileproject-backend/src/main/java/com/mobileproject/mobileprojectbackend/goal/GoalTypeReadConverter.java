package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

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
