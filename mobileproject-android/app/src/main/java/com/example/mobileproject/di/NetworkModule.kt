package com.example.mobileproject.di

import android.content.Context
import android.util.Log
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.datasource.remote.GoalApiService
import com.example.mobileproject.data.datasource.remote.PlaceApiService
import com.example.mobileproject.utils.ApiBaseUrlResolver
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

/**
 * Hilt DI module that provides all networking-related singletons.
 * Installed in [SingletonComponent] so every binding lives for the entire application lifecycle.
 *
 * Dependency graph produced by this module:
 *   Gson ─┐
 *         ├─► Retrofit ─┬─► ApiService
 *   OkHttpClient ───────┤──► PlaceApiService
 *                       └──► GoalApiService
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** HTTP cache max-age (seconds) applied only to GET /api/places responses. */
    private const val PLACE_CACHE_MAX_AGE_SECONDS = 120

    /** Maximum disk space (20 MB) for the OkHttp place-response cache. */
    private const val PLACE_HTTP_CACHE_SIZE_BYTES = 20L * 1024L * 1024L

    /**
     * Provides a shared [Gson] instance used by Retrofit's converter and anywhere else
     * that needs JSON serialization. Singleton avoids repeated parser allocations.
     */
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    /**
     * Provides a configured [OkHttpClient] with:
     * 1. An **application interceptor** that logs every outgoing request and incoming response
     *    (including error body previews for non-2xx responses) via Logcat.
     * 2. A **disk cache** (20 MB in the app's cache directory) for HTTP responses.
     * 3. A **network interceptor** that injects `Cache-Control: max-age=120` headers
     *    exclusively for GET requests to `/api/places`, enabling offline/short-term caching
     *    of place data without affecting other endpoints.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheDirectory = File(context.cacheDir, "http-place-cache")
        val cache = Cache(cacheDirectory, PLACE_HTTP_CACHE_SIZE_BYTES)

        return OkHttpClient.Builder()
            // Application-level interceptor: runs for every request (including cache hits).
            .addInterceptor { chain ->
                val request = chain.request()
                try {
                    Log.i("NetworkModule", "Outgoing request: ${request.method} ${request.url}")
                } catch (_: Throwable) {
                }
                val response: Response = chain.proceed(request)
                try {
                    Log.i(
                        "NetworkModule",
                        "Incoming response: ${response.code} ${request.method} ${request.url}"
                    )
                    if (!response.isSuccessful) {
                        // Peek at the error body without consuming it, so downstream readers still work.
                        val bodyPreview = response.peekBody(2048).string()
                        Log.w(
                            "NetworkModule",
                            "HTTP ${response.code} for ${request.url}. Body: $bodyPreview"
                        )
                    }
                } catch (_: Throwable) {
                }
                response
            }
            .cache(cache)
            // Network-level interceptor: runs only for requests that go to the network.
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)

                // Only cache GET responses for the places API to avoid stale data on other endpoints.
                val shouldCachePlaceGet =
                    request.method.equals("GET", ignoreCase = true) &&
                        request.url.encodedPath.startsWith("/api/places")

                if (!shouldCachePlaceGet) {
                    return@addNetworkInterceptor response
                }

                // Override the server's Cache-Control so OkHttp's disk cache stores the response.
                response.newBuilder()
                    .header("Cache-Control", "public, max-age=$PLACE_CACHE_MAX_AGE_SECONDS")
                    .build()
            }
            .build()
    }

    /**
     * Provides the Retrofit instance configured with:
     * - Base URL resolved from [BuildConfig.API_BASE_URL] via [ApiBaseUrlResolver]
     *   (automatically rewrites localhost/127.0.0.1 to the emulator host 10.0.2.2).
     * - The shared [OkHttpClient] for HTTP transport and logging.
     * - A [GsonConverterFactory] for JSON deserialization.
     */
    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        val configured = ApiBaseUrlResolver.resolveHttpBase(BuildConfig.API_BASE_URL)
        Log.i("NetworkModule", "Retrofit baseUrl=$configured")

        return Retrofit.Builder()
            .baseUrl(configured)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Provides the main [ApiService] (general-purpose REST endpoints: auth, products, etc.)
     * generated by Retrofit from the interface definition.
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    /**
     * Provides [PlaceApiService] for place-related REST endpoints (search, details, etc.).
     * Separated from [ApiService] to allow independent endpoint grouping and easier testing.
     */
    @Provides
    @Singleton
    fun providePlaceApiService(retrofit: Retrofit): PlaceApiService {
        return retrofit.create(PlaceApiService::class.java)
    }

    /**
     * Provides [GoalApiService] for financial goal REST endpoints (CRUD, progress tracking).
     */
    @Provides
    @Singleton
    fun provideGoalApiService(retrofit: Retrofit): GoalApiService {
        return retrofit.create(GoalApiService::class.java)
    }
}
