// ── Plugin Repositories ──────────────────────────────────────────────────────
// Controls where Gradle resolves plugin artifacts from.
pluginManagement {
    repositories {
        // Google's Maven repository – hosts Android Gradle Plugin, Firebase, and AndroidX.
        // Scoped to specific groups to avoid pulling non-Android artifacts from Google.
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()            // Primary repository for most open-source JVM libraries
        gradlePluginPortal()      // Gradle's own plugin portal (plugins.gradle.org)
    }
}

// ── JVM Toolchain Resolution ─────────────────────────────────────────────────
// Automatically downloads the required JDK version if not installed locally.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// ── Dependency Repositories ──────────────────────────────────────────────────
// Controls where dependency (non-plugin) artifacts are resolved from.
dependencyResolutionManagement {
    // FAIL_ON_PROJECT_REPOS: prevents individual modules from declaring their own
    // repositories, ensuring all resolution goes through this central config.
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// ── Project Structure ────────────────────────────────────────────────────────
rootProject.name = "MobileProject"
include(":app")  // The single Android application module