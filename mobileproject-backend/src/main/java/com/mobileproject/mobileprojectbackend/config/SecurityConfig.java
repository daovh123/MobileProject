package com.mobileproject.mobileprojectbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Cấu hình bảo mật cho ứng dụng, sử dụng JWT stateless authentication.
 *
 * <p>Chiến lược bảo mật:</p>
 * <ul>
 *   <li><b>Stateless session</b> - Không lưu session trên server, mỗi request phải tự chứa token</li>
 *   <li><b>JWT Filter</b> - {@link AuthTokenAuthenticationFilter} được đặt trước
 *       {@code UsernamePasswordAuthenticationFilter} để xác thực token từ header Authorization</li>
 *   <li><b>CSRF disabled</b> - Không cần CSRF vì sử dụng JWT (không có session cookie)</li>
 *   <li><b>BCrypt password encoding</b> - Mã hóa mật khẩu bằng BCryptPasswordEncoder</li>
 * </ul>
 *
 * <p>Cấu hình phân quyền URL:</p>
 * <ul>
 *   <li>API công khai: {@code /api/auth/**}, {@code /api/places/**}, {@code /api/favorites/**},
 *       {@code /api/history/**}, {@code /api/notifications/**}, {@code /api/v1/wallet/**},
 *       {@code /api/v1/transactions/**}, {@code /api/v1/goals/**}, {@code /api/v1/analytics/**},
 *       {@code /ws/**}, {@code /actuator/**}, {@code /error}</li>
 *   <li>Các URL còn lại: yêu cầu xác thực (authenticated)</li>
 * </ul>
 *
 * <p>Xử lý lỗi xác thực:</p>
 * <ul>
 *   <li>Không xác thực → HTTP 401 Unauthorized</li>
 *   <li>Không có quyền → HTTP 403 Forbidden</li>
 * </ul>
 */
@Configuration
public class SecurityConfig {

    /**
     * Cấu hình chuỗi lọc bảo mật (Security Filter Chain).
     *
     * @param http                          đối tượng HttpSecurity để cấu hình
     * @param authTokenAuthenticationFilter  JWT filter xác thực token, được inject từ Spring context
     * @return chuỗi lọc bảo mật đã cấu hình
     * @throws Exception nếu có lỗi trong quá trình cấu hình
     */
    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthTokenAuthenticationFilter authTokenAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 1. Chế độ Stateless cho JWT
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 2. Cấu hình phân quyền
                 .authorizeHttpRequests(auth -> auth
                         .requestMatchers(
                                 // Danh sách các API cho phép truy cập tự do (tổng hợp từ cả 2 nhánh)
                                 "/api/auth/**",
                                 "/api/places/**",
                                 "/api/favorites/**",
                                 "/api/history/**",
                                 "/api/notifications/**",
                                 "/api/v1/transactions/**",
                                 "/api/v1/top-ups/**",
                                 "/api/v1/topups/**",
                                 "/api/v1/payouts/**",
                                 "/api/v1/goals/**",
                                 "/api/v1/analytics/**",
                                 "/api/v1/wallet/**",
                                 "/ws/**",
                                 "/actuator/**",
                                 "/error"
                         ).permitAll()
                        .anyRequest().authenticated())

                // 3. Xử lý ngoại lệ (Trả về 401/403 thay vì redirect trang login)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpStatus.FORBIDDEN.value())))

                // 4. Tắt các filter mặc định không cần thiết
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // 5. Thêm JWT Filter vào chuỗi lọc
                .addFilterBefore(authTokenAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Bean mã hóa mật khẩu sử dụng BCrypt.
     *
     * @return PasswordEncoder sử dụng thuật toán BCrypt
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
