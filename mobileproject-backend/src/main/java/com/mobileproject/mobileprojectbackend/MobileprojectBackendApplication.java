package com.mobileproject.mobileprojectbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point của ứng dụng Spring Boot cho hệ thống quản lý cặp đôi (Mobile Project Backend).
 *
 * <p>Ứng dụng này cung cấp các REST API cho quản lý người dùng, xác thực, ghép đôi,
 * quản lý tài chính (ví chung), và tích hợp Firebase cho storage.</p>
 *
 * <p>Cấu hình chính:</p>
 * <ul>
 *   <li>{@code @SpringBootApplication} - Auto-configuration, component scanning, và property defaults</li>
 *   <li>{@code @EnableScheduling} - Kích hoạt hỗ trợ lập lịch (scheduled tasks) cho các tác vụ định kỳ
 *       như dọn dẹp cache hết hạn</li>
 * </ul>
 *
 * <p>Công nghệ sử dụng: Spring Boot 3, Spring Security (JWT stateless), MongoDB, Redis, Firebase Storage.</p>
 */
@SpringBootApplication
@EnableScheduling
public class MobileprojectBackendApplication {

    /**
     * Khởi chạy ứng dụng Spring Boot.
     *
     * @param args tham số dòng lệnh truyền vào khi khởi động
     */
    public static void main(String[] args) {
        SpringApplication.run(MobileprojectBackendApplication.class, args);
    }

}
