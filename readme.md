# AFFINITY Wallet

Ứng dụng quản lý tài chính và lối sống dành cho các cặp đôi. Quản lý ví chung, theo dõi chi tiêu, trò chuyện thời gian thực, chia sẻ vị trí, lưu giữ kỷ niệm và khám phá địa điểm cùng nhau.

## Mục lục

- [Tính năng](#tính-năng)
- [Cấu trúc dự án](#cấu-trúc-dự-án)
- [Công nghệ sử dụng](#công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#yêu-cầu-hệ-thống)
- [Cài đặt & Chạy dự án](#cài-đặt--chạy-dự-án)
- [Triển khai](#triển-khai)
- [Kiến trúc](#kiến-trúc)

## Tính năng

### Tài chính
- **Ví chung** — Số dư ví cặp đôi, lịch sử giao dịch, nạp tiền qua QR ngân hàng (SePay), chuyển tiền
- **Mục tiêu tiết kiệm** — Tạo mục tiêu tiết kiệm / mục tiêu tương lai, đóng góp từ ví hoặc trực tiếp, rút về ví
- **Theo dõi chi tiêu** — Ghi chi tiêu theo danh mục, phân tích bằng biểu đồ tròn, xu hướng theo tháng

### Giao tiếp
- **Chat thời gian thực** — Trò chuyện giữa hai người qua WebSocket, trả lời nhanh từ thông báo
- **Chia sẻ vị trí** — Chia sẻ vị trí thời gian thực giữa hai người qua foreground service + WebSocket
- **Thông báo đẩy** — FCM push notifications, nhắc nhở ngày đặc biệt

### Khám phá
- **Khám phá địa điểm** — Gợi ý địa điểm bằng AI (Groq/Llama3), tìm kiếm với bộ lọc, danh sách yêu thích
- **Kỷ niệm / Moments** — Chia sẻ khoảnh khắc (hình ảnh), reactions (emoji), bình luận, widget màn hình chính

### Cá nhân
- **Hồ sơ** — Chỉnh sửa hồ sơ, xem hồ sơ đối tác, tùy chỉnh avatar với khung hình
- **Kết nối cặp đôi** — Kết nối qua mã mời, quản lý trạng thái cặp đôi
- **Cài đặt** — Chế độ sáng/tắt, cài đặt thông báo

## Cấu trúc dự án

```
MobileProject/
├── mobileproject-android/          # Ứng dụng Android (Kotlin + Jetpack Compose)
│   ├── app/src/main/
│   │   ├── java/com/example/mobileproject/
│   │   │   ├── core/               # Utilities dùng chung
│   │   │   ├── data/               # Layer data (repository, API, datastore)
│   │   │   ├── di/                 # Dagger Hilt modules
│   │   │   ├── domain/             # Layer domain (entities, use cases)
│   │   │   ├── presentation/       # Layer UI (screens, viewmodels, navigation)
│   │   │   └── utils/              # Helpers
│   │   └── res/                    # Resources
│   └── ARCHITECTURE.md             # Tài liệu kiến trúc Clean Architecture
│
├── mobileproject-backend/          # Backend API (Java 21 + Spring Boot)
│   ├── src/main/java/.../
│   │   ├── analytics/              # Phân tích chi tiêu
│   │   ├── auth/                   # Xác thực, hồ sơ, cặp đôi, ví
│   │   ├── chat/                   # Chat WebSocket + Groq AI
│   │   ├── config/                 # Security, caching, MongoDB
│   │   ├── favorite/               # Địa điểm yêu thích
│   │   ├── goal/                   # Mục tiêu tiết kiệm, nhiệm vụ
│   │   ├── map/                    # Chia sẻ vị trí thời gian thực
│   │   ├── moment/                 # Kỷ niệm (bài viết, reactions, bình luận)
│   │   ├── notifications/          # FCM push notifications
│   │   ├── payout/                 # Rút tiền qua SePay
│   │   ├── place/                  # Khám phá địa điểm
│   │   ├── storage/                # Firebase Storage (avatar)
│   │   ├── topup/                  # Nạp tiền qua SePay QR
│   │   └── transaction/            # Giao dịch tài chính
│   ├── Dockerfile
│   └── pom.xml
│
├── render.yaml                     # Cấu hình triển khai Render.com
└── README.md
```

## Công nghệ sử dụng

### Android

| Hạng mục | Công nghệ |
|---|---|
| Ngôn ngữ | Kotlin (JVM 11) |
| UI | Jetpack Compose + Material Design 3 |
| Kiến trúc | Clean Architecture (Presentation / Domain / Data) |
| Dependency Injection | Dagger Hilt |
| Mạng | Retrofit + OkHttp + Gson |
| Hình ảnh | Coil |
| Bản đồ | osmdroid (OpenStreetMap) |
| Camera / QR | CameraX + ML Kit Barcode Scanning |
| Cơ sở dữ liệu cục bộ | DataStore Preferences |
| Push Notifications | Firebase Cloud Messaging |
| Widget | Glance |

### Backend

| Hạng mục | Công nghệ |
|---|---|
| Ngôn ngữ | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Cơ sở dữ liệu | MongoDB (Spring Data MongoDB) |
| Cache | Redis |
| Bảo mật | Spring Security + JWT |
| Thời gian thực | Spring WebSocket |
| AI | Groq API (Llama3-8b-8192) |
| Thanh toán | SePay (QR ngân hàng) |
| Storage | Firebase Storage |
| Triển khai | Docker trên Render.com |

## Yêu cầu hệ thống

### Android
- Android Studio Hedgehog (2023.1.1) trở lên
- JDK 11+
- Android SDK 36
- Thiết bị Android 7.0 (API 24) trở lên

### Backend
- JDK 21
- Maven 3.9+
- MongoDB (Atlas hoặc local)
- Redis
- Tài khoản Firebase (cho push notifications và storage)
- API key Groq (cho tính năng AI khám phá)
- Tài khoản SePay (cho thanh toán ngân hàng)

## Cài đặt & Chạy dự án

### Backend

1. Clone repository và di chuyển vào thư mục backend:
   ```bash
   cd mobileproject-backend
   ```

2. Tạo file `secrets.properties` trong thư mục gốc backend:
   ```properties
   # MongoDB
   spring.data.mongodb.uri=mongodb+srv://<user>:<password>@<cluster>/<database>

   # Redis
   spring.data.redis.host=<redis-host>
   spring.data.redis.port=6379
   spring.data.redis.password=<redis-password>

   # JWT
   jwt.secret=<your-jwt-secret>

   # Firebase
   firebase.service-account.path=secrets/firebase-service-account.json

   # Groq AI
   groq.api.key=<your-groq-api-key>

   # SePay
   sepay.api.key=<your-sepay-api-key>
   sepay.api.url=<sepay-api-url>
   ```

3. Chạy ứng dụng:
   ```bash
   mvn spring-boot:run
   ```

   Backend sẽ chạy tại `http://localhost:8080`

### Android

1. Di chuyển vào thư mục Android:
   ```bash
   cd mobileproject-android
   ```

2. (Tùy chọn) Cấu hình URL backend trong `gradle.properties`:
   ```properties
   API_BASE_URL=http://10.0.2.2:8080/
   ```

3. Mở project trong Android Studio, sync Gradle và chạy trên thiết bị hoặc emulator.

## Triển khai

Backend được triển khai trên **Render.com** sử dụng Docker:

1. Cấu hình biến môi trường theo `env.render.sepay.example.yml`
2. Deploy tự động qua `render.yaml`
3. Health check endpoint: `/actuator/health`

## Kiến trúc

Ứng dụng Android tuân theo **Clean Architecture** với 3 tầng:

```
Presentation (UI) → Domain (Business Logic) → Data (Repository/API)
```

Chi tiết kiến trúc xem tại [`mobileproject-android/ARCHITECTURE.md`](mobileproject-android/ARCHITECTURE.md).
