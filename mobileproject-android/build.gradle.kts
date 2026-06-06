// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Plugins are declared here with `apply false` so they are resolved once and applied
// selectively in each module's own build.gradle.kts.

plugins {
    // Android Gradle Plugin – declared in version catalog (libs.versions.toml).
    alias(libs.plugins.android.application) apply false

    // ── Dependency Injection ──────────────────────────────────────────────────
    // Dagger Hilt – compile-time DI framework for Android.
    id("com.google.dagger.hilt.android") version "2.59.2" apply false

    // ── Annotation Processing ────────────────────────────────────────────────
    // Kotlin Symbol Processing (KSP) – used by Hilt to generate DI code at compile time.
    id("com.google.devtools.ksp") version "2.3.2" apply false

    // ── Firebase ─────────────────────────────────────────────────────────────
    // Google Services plugin – processes google-services.json for Firebase/FCM.
    id("com.google.gms.google-services") version "4.4.4" apply false
}