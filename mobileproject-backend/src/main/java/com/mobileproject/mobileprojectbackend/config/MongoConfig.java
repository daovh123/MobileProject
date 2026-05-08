package com.mobileproject.mobileprojectbackend.config;

import com.mobileproject.mobileprojectbackend.goal.GoalTypeReadConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(GoalTypeReadConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }

}
