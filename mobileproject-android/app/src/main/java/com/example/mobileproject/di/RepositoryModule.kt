package com.example.mobileproject.di

import com.example.mobileproject.data.repository.*
import com.example.mobileproject.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module that binds every domain-layer [Repository] interface to its concrete
 * [RepositoryImpl] implementation. All bindings are singletons scoped to the application
 * lifecycle via [SingletonComponent].
 *
 * [@Binds] is preferred over [@Provides] here because each binding is a simple
 * interface-to-implementation mapping with no construction logic—Hilt generates the
 * forwarding code at compile time, resulting in zero runtime overhead.
 *
 * Injected dependencies (ApiService, DAOs, etc.) are constructor-injected into each *Impl
 * class; Hilt resolves them automatically.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /** Binds the authentication repository (login, register, token management). */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl,
    ): AuthRepository

    /** Binds the onboarding repository (first-launch user preferences flow). */
    @Binds
    @Singleton
    abstract fun bindOnboardingRepository(
        impl: OnboardingRepositoryImpl,
    ): OnboardingRepository

    /** Binds the product repository (product listing, details). */
    @Binds
    @Singleton
    abstract fun bindProductRepository(
        impl: ProductRepositoryImpl,
    ): ProductRepository

    /** Binds the place repository (place search, details, caching). */
    @Binds
    @Singleton
    abstract fun bindPlaceRepository(
        impl: PlaceRepositoryImpl,
    ): PlaceRepository

    /** Binds the favorite repository (user-saved/favorited items). */
    @Binds
    @Singleton
    abstract fun bindFavoriteRepository(
        impl: FavoriteRepositoryImpl,
    ): FavoriteRepository

    /** Binds the history repository (browsing/visit history tracking). */
    @Binds
    @Singleton
    abstract fun bindHistoryRepository(
        impl: HistoryRepositoryImpl,
    ): HistoryRepository

    /** Binds the transaction repository (payment/transfer transaction records). */
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl,
    ): TransactionRepository

    /** Binds the goal repository (financial goal CRUD and progress tracking). */
    @Binds
    @Singleton
    abstract fun bindGoalRepository(
        impl: GoalRepositoryImpl,
    ): GoalRepository

    /** Binds the analytics repository (spending analytics and reports). */
    @Binds
    @Singleton
    abstract fun bindAnalyticsRepository(
        impl: AnalyticsRepositoryImpl,
    ): AnalyticsRepository

    /** Binds the wallet repository (balance, wallet info). */
    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        impl: WalletRepositoryImpl
    ): WalletRepository

    /** Binds the top-up repository (bank-transfer top-up via SePay). */
    @Binds
    @Singleton
    abstract fun bindTopUpRepository(
        impl: TopUpRepositoryImpl,
    ): TopUpRepository

    /** Binds the payout repository (withdrawal/payout operations). */
    @Binds
    @Singleton
    abstract fun bindPayoutRepository(
        impl: PayoutRepositoryImpl,
    ): PayoutRepository
}
