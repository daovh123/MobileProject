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

@Configuration
public class SecurityConfig {

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

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
