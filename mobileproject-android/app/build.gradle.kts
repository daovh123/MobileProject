// ── Plugins ───────────────────────────────────────────────────────────────────
plugins {
    alias(libs.plugins.android.application)      // Android app module (AGP)
    alias(libs.plugins.kotlin.compose)           // Jetpack Compose Kotlin compiler plugin
    id("com.google.dagger.hilt.android")         // Hilt DI code generation
    id("com.google.devtools.ksp")                // KSP annotation processing (Hilt compiler)
    id("com.google.gms.google-services") apply false  // Firebase – applied conditionally below

}

// ── Android Configuration ─────────────────────────────────────────────────────
android {
    namespace = "com.example.mobileproject"

    // API base URL injected via project property (-PAPI_BASE_URL=...) or defaults to localhost.
    // Runtime resolution (emulator host rewriting) is handled by ApiBaseUrlResolver.
    val apiBaseUrl = ((project.findProperty("API_BASE_URL") as? String)
        ?: "http://localhost:8080/")
        .trim()
        .removeSurrounding("\"")

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mobileproject"
        minSdk = 24          // Android 7.0 (Nougat) – minimum supported API level
        targetSdk = 36       // Compiled against Android 16 APIs
        versionCode = 1      // Integer version for Play Store (increment per release)
        versionName = "1.0"  // Human-readable version string

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Exposes API_BASE_URL as BuildConfig.API_BASE_URL in Kotlin/Java source.
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    // ── Build Types ──────────────────────────────────────────────────────────
    buildTypes {
        debug {
            // Allow HTTP (cleartext) traffic for local dev servers running on http://.
            manifestPlaceholders["cleartextTrafficPermitted"] = "true"
            lint {
                checkReleaseBuilds = false
                abortOnError = false
            }
        }
        release {
            isMinifyEnabled = false  // TODO: enable R8 minification for production
            // Block cleartext HTTP in release; all traffic must use TLS.
            manifestPlaceholders["cleartextTrafficPermitted"] = "false"
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ── Java Compatibility ───────────────────────────────────────────────────
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    // ── Build Features ───────────────────────────────────────────────────────
    buildFeatures {
        compose = true     // Enable Jetpack Compose compiler
        buildConfig = true // Generate BuildConfig class (for API_BASE_URL)
    }

    // ── Custom Test Source Set ────────────────────────────────────────────────
    // Redirects test sources to a non-default directory structure.
    sourceSets {
        getByName("test") {
            java.srcDirs("src/test/single/java")
            resources.srcDirs("src/test/single/resources")
        }
    }
}

// ── Kotlin Compiler Options ───────────────────────────────────────────────────
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// ── Dependencies ──────────────────────────────────────────────────────────────
dependencies {

    // ─── Jetpack Compose UI ──────────────────────────────────────────────────
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)                              // Material Design components
    implementation(libs.appcompat)                             // AppCompat backward compat
    implementation(libs.androidx.activity.compose)             // ComponentActivity + Compose
    implementation(platform(libs.androidx.compose.bom))        // Compose BOM for version alignment
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)            // Material 3 design system
    implementation(libs.androidx.compose.foundation.layout)
    implementation("androidx.compose.ui:ui-text-google-fonts") // Google Fonts in Compose
    debugImplementation(libs.androidx.compose.ui.tooling)      // Preview & layout inspector

    // ─── Navigation & Image Loading ──────────────────────────────────────────
    implementation("androidx.navigation:navigation-compose:2.8.7") // Compose navigation
    implementation("io.coil-kt:coil-compose:2.7.0")                // Coil image loader for Compose
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.datastore:datastore-preferences:1.1.7") // Key-value persistence

    // ─── Networking (Retrofit + OkHttp) ──────────────────────────────────────
    implementation("com.squareup.retrofit2:retrofit:2.11.0")       // Type-safe HTTP client
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // JSON deserialization
    implementation("com.squareup.okhttp3:okhttp:4.12.0")           // HTTP engine

    // ─── Maps ────────────────────────────────────────────────────────────────
    implementation("org.osmdroid:osmdroid-android:6.1.18")         // OpenStreetMap tiles

    // ─── Android Lifecycle & Async ───────────────────────────────────────────
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")   // ProcessLifecycleOwner
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("io.coil-kt:coil:2.7.0")

    // ─── Location & Camera / ML Kit ─────────────────────────────────────────
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.camera:camera-core:1.5.3")
    implementation("androidx.camera:camera-camera2:1.5.3")
    implementation("androidx.camera:camera-lifecycle:1.5.3")
    implementation("androidx.camera:camera-view:1.0.0-alpha34")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")     // On-device QR/barcode scanning

    // ─── Testing ─────────────────────────────────────────────────────────────
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("io.mockk:mockk:1.13.12")                   // Kotlin mocking framework
    testImplementation("androidx.test:core-ktx:1.6.1")
    testImplementation("org.robolectric:robolectric:4.12.2")       // JVM-based Android tests
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ─── Dependency Injection (Hilt) ─────────────────────────────────────────
    implementation("com.google.dagger:hilt-android:2.59.2")
    ksp("com.google.dagger:hilt-compiler:2.59.2")                  // Hilt annotation processor
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")  // hiltViewModel() in Compose

    // ─── App Widgets (Glance) ────────────────────────────────────────────────
    implementation("androidx.glance:glance:1.1.0")
    implementation("androidx.glance:glance-appwidget:1.1.0")       // Compose-based app widgets

    // ─── Firebase ────────────────────────────────────────────────────────────
    implementation(platform("com.google.firebase:firebase-bom:34.12.0")) // BOM for version alignment
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-messaging")             // Push notifications (FCM)

    // ─── QR Code Generation ──────────────────────────────────────────────────
    implementation("com.google.zxing:core:3.5.3")                 // ZXing barcode/QR encoder
}

// ── Conditional Firebase Setup ────────────────────────────────────────────────
// Only apply the Google Services plugin if a google-services.json file exists.
// This allows building the project (e.g. on CI or for contributors) without
// Firebase credentials—the app will compile but FCM/analytics will be unavailable.
val hasGoogleServicesConfig = file("google-services.json").exists() ||
    file("src\\debug\\google-services.json").exists() ||
    file("src\\release\\google-services.json").exists()

if (hasGoogleServicesConfig) {
    apply(plugin = "com.google.gms.google-services")
}
