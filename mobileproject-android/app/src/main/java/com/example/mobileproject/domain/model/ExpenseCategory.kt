package com.example.mobileproject.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*

sealed class ExpenseCategory(
    val id: String,
    val displayName: String,
    val icon: ImageVector,
    val type: CategoryType
) {
    enum class CategoryType {
        ESSENTIALS,
        LIFESTYLE,
        FINANCIALS
    }

    // Essentials
    object FoodDrink : ExpenseCategory("FOOD_DRINK", "Food & Drink", Icons.Default.Restaurant, CategoryType.ESSENTIALS)
    object Transport : ExpenseCategory("TRANSPORT", "Transport", Icons.Default.DirectionsCar, CategoryType.ESSENTIALS)
    object Household : ExpenseCategory("HOUSEHOLD", "Household", Icons.Default.Home, CategoryType.ESSENTIALS)
    object Bills : ExpenseCategory("BILLS", "Bills", Icons.Default.Receipt, CategoryType.ESSENTIALS)
    object Rent : ExpenseCategory("RENT", "Rent", Icons.Default.Apartment, CategoryType.ESSENTIALS)
    object Education : ExpenseCategory("EDUCATION", "Education", Icons.Default.School, CategoryType.ESSENTIALS)

    // Couple & Lifestyle
    object Dating : ExpenseCategory("DATING", "Dating", Icons.Default.Favorite, CategoryType.LIFESTYLE)
    object Gifts : ExpenseCategory("GIFTS", "Gifts", Icons.Default.CardGiftcard, CategoryType.LIFESTYLE)
    object Travel : ExpenseCategory("TRAVEL", "Travel", Icons.Default.Flight, CategoryType.LIFESTYLE)
    object Pet : ExpenseCategory("PET", "Pet", Icons.Default.Pets, CategoryType.LIFESTYLE)
    object Entertainment : ExpenseCategory("ENTERTAINMENT", "Entertainment", Icons.Default.Movie, CategoryType.LIFESTYLE)

    // Financials
    object Emergency : ExpenseCategory("EMERGENCY", "Emergency", Icons.Default.Error, CategoryType.FINANCIALS)
    object Investment : ExpenseCategory("INVESTMENT", "Investment", Icons.AutoMirrored.Filled.TrendingUp, CategoryType.FINANCIALS)
    object Others : ExpenseCategory("OTHERS", "Others", Icons.Default.Category, CategoryType.FINANCIALS)

    companion object {
        // Sử dụng function để tránh lỗi null khi khởi tạo companion object
        fun getAll(): List<ExpenseCategory> = listOf(
            FoodDrink, Transport, Household, Bills, Rent, Education,
            Dating, Gifts, Travel, Pet, Entertainment,
            Emergency, Investment, Others
        )
        
        fun fromId(id: String): ExpenseCategory = getAll().find { it.id == id } ?: Others
    }
}
