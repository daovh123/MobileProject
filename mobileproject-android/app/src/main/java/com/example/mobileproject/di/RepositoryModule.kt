package com.example.mobileproject.di

import com.example.mobileproject.data.repository.*
import com.example.mobileproject.domain.repository.*
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

    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        impl: FavoriteRepositoryImpl,
    ): FavoriteRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl,
    ): HistoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl,
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        impl: GoalRepositoryImpl,
    ): GoalRepository

    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl,
    ): AnalyticsRepository

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        impl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindTopUpRepository(
        impl: TopUpRepositoryImpl,
    ): TopUpRepository
}
