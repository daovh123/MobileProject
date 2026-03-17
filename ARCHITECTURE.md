# Clean Architecture structure (MobileProject)

Tài liệu này mô tả cấu trúc thư mục theo Clean Architecture đã được scaffold trong dự án.

## Mục tiêu

- Tách biệt rõ **Presentation / Domain / Data** để dễ mở rộng và test.
- Domain không phụ thuộc Android framework hay thư viện UI.
- Data triển khai chi tiết (API/DB) và mapping qua Domain.
- Giữ các thư mục rỗng bằng `.gitkeep` để có thể commit lên GitHub.

## Cây thư mục (main)

Gốc package: `app/src/main/java/com/example/mobileproject`

```text
com/example/mobileproject/
  core/
    common/
    dispatcher/
    extension/
    result/
    ui/
  data/
    datasource/
      local/
      remote/
    mapper/
    model/
      local/
      remote/
    repository/
  domain/
    entity/
    repository/
    usecase/
  presentation/
    navigation/
    state/
    ui/
      components/
      screen/
    viewmodel/
  di/
  utils/
```

## Chức năng từng folder / từng phần

### 1) Presentation (`presentation/*`)

Vai trò:
- Chứa toàn bộ phần hiển thị UI và xử lý tương tác người dùng.
- Nhận input từ UI -> chuyển thành event -> gọi use case ở Domain.
- Tuyệt đối **không** gọi trực tiếp Retrofit/Room/API service.

Chi tiết từng folder:
- `presentation/ui/screen/*`
  - Màn hình hoàn chỉnh (ví dụ: Home, Detail, Profile).
  - Nhiệm vụ chính: render theo `UiState`, phát event về `ViewModel`.
- `presentation/ui/components/*`
  - Các component tái sử dụng (button/card/input/app bar/custom item).
  - Không chứa business logic, ưu tiên stateless.
- `presentation/viewmodel/*`
  - Quản lý state cho màn hình, xử lý event từ UI.
  - Gọi `domain/usecase/*`, map kết quả sang `UiState`.
- `presentation/state/*`
  - Khai báo `UiState`, `UiEvent`, có thể thêm `UiEffect` nếu dùng one-shot event.
  - Mục tiêu: chuẩn hóa luồng dữ liệu một chiều trong UI.
- `presentation/navigation/*`
  - Định nghĩa route, nav graph, argument điều hướng.
  - Tách navigation logic khỏi code UI cụ thể của từng màn.

### 2) Domain (`domain/*`)

Vai trò:
- Là lõi nghiệp vụ của app, viết thuần Kotlin.
- Không phụ thuộc Android framework, Retrofit, Room hay thư viện UI.

Chi tiết từng folder:
- `domain/entity/*`
  - Mô hình dữ liệu nghiệp vụ cốt lõi dùng xuyên suốt app.
  - Đại diện cho “ngôn ngữ nghiệp vụ”, không đại diện cho DTO API.
- `domain/repository/*`
  - Interface contract cho các thao tác dữ liệu (fetch/save/update/delete...).
  - Chỉ định “cần gì”, không nói “làm như thế nào”.
- `domain/usecase/*`
  - Mỗi file tương ứng một hành động nghiệp vụ.
  - Điều phối rule nghiệp vụ, gọi repository interface, trả kết quả cho Presentation.

Nguyên tắc quan trọng:
- Domain không import Data/Presentation.
- Use case chỉ phụ thuộc `domain/repository/*` (interface).

### 3) Data (`data/*`)

Vai trò:
- Triển khai chi tiết cách lấy/lưu dữ liệu từ nguồn local và remote.
- Chuyển đổi dữ liệu datasource về domain entity để trả cho Domain/Presentation.

Chi tiết từng folder:
- `data/datasource/local/*`
  - Truy cập dữ liệu local: Room DAO, DataStore, file cache...
  - Chỉ làm việc với local model/entity của local layer.
- `data/datasource/remote/*`
  - Truy cập network: API service (Retrofit/Ktor/GraphQL client).
  - Nhận/trả remote DTO.
- `data/model/local/*`
  - Model dành cho local storage (DB entity, preference model...).
- `data/model/remote/*`
  - DTO request/response từ API.
- `data/mapper/*`
  - Chuyển đổi qua lại giữa `model/*` và `domain/entity/*`.
  - Giúp domain không bị rò rỉ chi tiết datasource.
- `data/repository/*`
  - Class implement `domain/repository/*`.
  - Kết hợp local/remote, xử lý cache strategy, error mapping.

Nguyên tắc quan trọng:
- Data phụ thuộc Domain để implement interface và trả về domain entity.

### 4) Core (`core/*`)

Vai trò:
- Chứa thành phần dùng chung toàn ứng dụng, không gắn với một feature cụ thể.

Chi tiết từng folder:
- `core/common/*`: hằng số chung, base type, utility dùng toàn app.
- `core/dispatcher/*`: abstraction cho coroutine dispatcher để dễ test.
- `core/extension/*`: extension function dùng lại nhiều nơi.
- `core/result/*`: wrapper trạng thái (Success/Error/Loading) hoặc tương đương.
- `core/ui/*`: thành phần UI base dùng chung (theme/base component/style contract).

### 5) DI (`di/*`)

Vai trò:
- Khai báo dependency injection modules (Hilt/Koin).
- Nơi bind interface -> implementation, provide API client, DB, dispatcher, repository, use case.

Mục tiêu:
- Tập trung cấu hình wiring ở một chỗ, tránh khởi tạo thủ công rải rác.

### 6) Utils (`utils/*`)

Vai trò:
- Các helper nhỏ, không thuộc nghiệp vụ chính.

Lưu ý:
- Không đặt business logic trong `utils/*`.
- Nếu helper chỉ dùng cho một layer cụ thể, cân nhắc đặt vào layer đó thay vì đưa vào utils.

## Luồng dữ liệu chuẩn (end-to-end)

1. User thao tác trên `presentation/ui/screen/*`.
2. Screen gửi event vào `presentation/viewmodel/*`.
3. ViewModel gọi `domain/usecase/*`.
4. UseCase gọi `domain/repository/*` (interface).
5. `data/repository/*` implement interface, gọi local/remote datasource.
6. `data/mapper/*` map dữ liệu sang `domain/entity/*`.
7. Kết quả trả về ViewModel -> cập nhật `UiState` -> UI render lại.

## Test folders

Đã tạo thêm các folder theo cùng cấu trúc trong:

- `app/src/test/java/com/example/mobileproject/*`
- `app/src/androidTest/java/com/example/mobileproject/*`

Bạn có thể đặt unit test cho Domain/Data trong `test/` và instrumented test trong `androidTest/`.

## Ghi chú về `.gitkeep`

- Git không track thư mục rỗng.
- Các file `.gitkeep` được đặt trong các thư mục mới tạo để đảm bảo chúng xuất hiện khi commit.
- Khi bạn tạo file code thật trong thư mục đó, có thể giữ hoặc xoá `.gitkeep` đều được.
