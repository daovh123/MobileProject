package com.example.mobileproject.di

import android.content.Context
import com.example.mobileproject.BuildConfig
import com.example.mobileproject.data.datasource.remote.ApiService
import com.example.mobileproject.data.datasource.remote.PlaceApiService
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val PLACE_CACHE_MAX_AGE_SECONDS = 120
    private const val PLACE_HTTP_CACHE_SIZE_BYTES = 20L * 1024L * 1024L

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        val cacheDirectory = File(context.cacheDir, "http-place-cache")
        val cache = Cache(cacheDirectory, PLACE_HTTP_CACHE_SIZE_BYTES)

        return OkHttpClient.Builder()
            .cache(cache)
            .addNetworkInterceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)

                val shouldCachePlaceGet =
                    request.method.equals("GET", ignoreCase = true) &&
                        request.url.encodedPath.startsWith("/api/places")

                if (!shouldCachePlaceGet) {
                    return@addNetworkInterceptor response
                }

                response.newBuilder()
                    .header("Cache-Control", "public, max-age=$PLACE_CACHE_MAX_AGE_SECONDS")
                    .build()
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlaceApiService(retrofit: Retrofit): PlaceApiService {
        return retrofit.create(PlaceApiService::class.java)
    }
}
