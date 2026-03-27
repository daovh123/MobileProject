# Tổng quan Kiến trúc Dự án (MobileProject)

Dự án này được tổ chức theo cấu trúc Monorepo, bao gồm 2 phần chính:

## 1. \mobileproject-android/\`n- Ứng dụng Mobile viết bằng **Kotlin** và **Jetpack Compose**.
- Tuân thủ mẫu thiết kế **Clean Architecture** (Presentation - Domain - Data).
- Chi tiết xem tại: [\mobileproject-android/ARCHITECTURE.md\](./mobileproject-android/ARCHITECTURE.md).

## 2. \mobileproject-backend/\`n- Máy chủ Backend viết bằng **Java** và framework **Spring Boot**.
- Cấu trúc RESTful API với các lớp Controller / Service / Repository.

