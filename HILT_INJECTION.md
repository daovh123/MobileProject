# Hilt Injection Guide (MobileProject)

Tài liệu này hướng dẫn cài đặt và “wiring” Hilt DI theo đúng cấu hình hiện tại của repo (Clean Architecture: `presentation/` → `domain/` → `data/` + `di/`).

## 0) Yêu cầu môi trường

- Android SDK đã cài (đã có `sdk.dir` trong `local.properties`).
- JDK đầy đủ có `jlink` (khuyến nghị dùng JBR của Android Studio).
  - Repo đang cấu hình trong `gradle.properties`:
    - `org.gradle.java.home=C:\Program Files\Android\Android Studio\jbr`

> Nếu build báo lỗi liên quan `jlink.exe does not exist`, xem phần **Troubleshooting**.

## 1) Gradle setup (Hilt + KSP)

### 1.1 Root build.gradle.kts

File: `build.gradle.kts`

- Khai báo version cho plugin Hilt và KSP (apply false):

```kotlin
plugins {
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.dagger.hilt.android") version "2.59.2" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false
}
```

### 1.2 App module build.gradle.kts

File: `app/build.gradle.kts`

- Apply plugins:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}
```

- Thêm dependencies Hilt (đúng artifact compiler):

```kotlin
dependencies {
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")
}
```

- Đồng bộ JVM target cho Kotlin/Java (để tránh mismatch khi build):

```kotlin
android {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}
```

## 2) Khởi tạo Application Class

Tạo file ở package root (ngang hàng `core/`, `data/`, `domain/`, `di/`):

File: `app/src/main/java/com/example/mobileproject/MainApplication.kt`

```kotlin
@HiltAndroidApp
class MainApplication : Application()
```

Khai báo trong Manifest:

File: `app/src/main/AndroidManifest.xml`

```xml
<application
    android:name=".MainApplication"
    ... />
```

## 3) Tạo các Module trong di/

### 3.1 NetworkModule (cấp cho data/remote)

File: `app/src/main/java/com/example/mobileproject/di/NetworkModule.kt`

- Nhiệm vụ: tạo `Retrofit` và `ApiService`.
- `@InstallIn(SingletonComponent::class)` để tồn tại xuyên vòng đời app.

Ví dụ:

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit = ...

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)
}
```

### 3.2 RepositoryModule (kết nối data ↔ domain)

File: `app/src/main/java/com/example/mobileproject/di/RepositoryModule.kt`

- Dùng `@Binds` để map interface Domain → implementation Data.

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl,
    ): ProductRepository
}
```

## 4) Inject theo từng tầng

### 4.1 Data layer

- `ApiService` được provide từ `NetworkModule`.
- `RemoteDataSource` nhận `ApiService` qua constructor injection:

```kotlin
class RemoteDataSource @Inject constructor(
    private val apiService: ApiService,
)
```

- Repository implementation nhận `RemoteDataSource`:

```kotlin
class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
) : ProductRepository
```

### 4.2 Domain layer

Use case nhận repository (interface) từ Domain:

```kotlin
class GetProductUseCase @Inject constructor(
    private val repository: ProductRepository,
)
```

### 4.3 Presentation layer

#### ViewModel

```kotlin
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val getProductUseCase: GetProductUseCase,
) : ViewModel()
```

#### Activity

- Activity muốn inject / dùng Hilt ViewModel thì phải có `@AndroidEntryPoint`.

```kotlin
@AndroidEntryPoint
class HomeActivity : AppCompatActivity()
```

- Với XML View system, có thể lấy VM như sau:

```kotlin
val vm = ViewModelProvider(this)[ProductViewModel::class.java]
```

#### Jetpack Compose (nếu dùng)

```kotlin
@Composable
fun ProductScreen(
    viewModel: ProductViewModel = hiltViewModel(),
) {
    // UI...
}
```

## 5) Quyền INTERNET (rất hay quên)

Nếu ViewModel/Repository gọi network (OkHttp/Retrofit) mà Manifest thiếu quyền internet, app có thể crash với lỗi dạng:

- `java.lang.SecurityException: Permission denied (missing INTERNET permission?)`

Fix:

File: `app/src/main/AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
```

## 6) Troubleshooting

### 6.1 Build lỗi liên quan `jlink`

Triệu chứng:
- `jlink executable ...\bin\jlink.exe does not exist`

Cách xử lý:
- Dùng JDK/JBR đầy đủ (Android Studio JBR), và trỏ Gradle vào JDK đó:
  - `org.gradle.java.home=C:\Program Files\Android\Android Studio\jbr`

### 6.2 Hilt lỗi do Kotlin metadata version

Triệu chứng:
- `Provided Metadata instance has version ... while maximum supported ...`

Cách xử lý:
- Nâng Hilt lên bản mới (repo đang dùng `2.59.2`).

### 6.3 Kapt vs KSP

- Với Kotlin 2.x + AGP mới, KSP thường ổn định hơn.
- Repo hiện đang dùng KSP (`ksp("com.google.dagger:hilt-compiler:...")`).

## 7) Lệnh build nhanh

- Build debug:
  - `./gradlew :app:assembleDebug`
- Cài lên emulator/device:
  - `./gradlew :app:installDebug`
