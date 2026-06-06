package com.mobileproject.mobileprojectbackend.config;

import com.mobileproject.mobileprojectbackend.goal.GoalTypeReadConverter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

/**
 * Cấu hình custom conversions cho MongoDB.
 *
 * <p>Đăng ký các {@link Converter} tùy chỉnh để chuyển đổi dữ liệu giữa Java và MongoDB.
 * Hiện tại đăng ký:</p>
 * <ul>
 *   <li>{@link GoalTypeReadConverter} - Chuyển đổi giá trị enum GoalType từ MongoDB sang Java</li>
 * </ul>
 *
 * <p>Lưu ý: Method {@link #mongoCustomConversions()} không có annotation {@code @Bean} -
 * cần kiểm tra lại cấu hình nếu converter không được kích hoạt.</p>
 */
@Configuration
public class MongoConfig {

    /**
     * Tạo danh sách custom conversions cho MongoDB.
     *
     * @return đối tượng MongoCustomConversions chứa các converter đã đăng ký
     */
    public MongoCustomConversions mongoCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(GoalTypeReadConverter.INSTANCE);
        return new MongoCustomConversions(converters);
    }

}
