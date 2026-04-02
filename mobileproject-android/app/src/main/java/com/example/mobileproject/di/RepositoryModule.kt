package com.example.mobileproject.di

import com.example.mobileproject.data.repository.AuthRepositoryImpl
import com.example.mobileproject.data.repository.OnboardingRepositoryImpl
import com.example.mobileproject.data.repository.PlaceRepositoryImpl
import com.example.mobileproject.data.repository.ProductRepositoryImpl
import com.example.mobileproject.domain.repository.AuthRepository
import com.example.mobileproject.domain.repository.OnboardingRepository
import com.example.mobileproject.domain.repository.PlaceRepository
import com.example.mobileproject.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl,
    ): OnboardingRepository

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl,
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindPlaceRepository(
        impl: PlaceRepositoryImpl,
    ): PlaceRepository
}
