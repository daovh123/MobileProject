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
    object FoodDrink : ExpenseCategory("FOOD_DRINK", "Ăn uống", Icons.Default.Restaurant, CategoryType.ESSENTIALS)
    object Transport : ExpenseCategory("TRANSPORT", "Di chuyển", Icons.Default.DirectionsCar, CategoryType.ESSENTIALS)
    object Household : ExpenseCategory("HOUSEHOLD", "Gia dụng", Icons.Default.Home, CategoryType.ESSENTIALS)
    object Bills : ExpenseCategory("BILLS", "Hóa đơn", Icons.Default.Receipt, CategoryType.ESSENTIALS)
    object Rent : ExpenseCategory("RENT", "Tiền nhà", Icons.Default.Apartment, CategoryType.ESSENTIALS)
    object Education : ExpenseCategory("EDUCATION", "Giáo dục", Icons.Default.School, CategoryType.ESSENTIALS)

    // Couple & Lifestyle
    object Dating : ExpenseCategory("DATING", "Hẹn hò", Icons.Default.Favorite, CategoryType.LIFESTYLE)
    object Gifts : ExpenseCategory("GIFTS", "Quà tặng", Icons.Default.CardGiftcard, CategoryType.LIFESTYLE)
    object Travel : ExpenseCategory("TRAVEL", "Du lịch", Icons.Default.Flight, CategoryType.LIFESTYLE)
    object Pet : ExpenseCategory("PET", "Thú cưng", Icons.Default.Pets, CategoryType.LIFESTYLE)
    object Entertainment : ExpenseCategory("ENTERTAINMENT", "Giải trí", Icons.Default.Movie, CategoryType.LIFESTYLE)

    // Financials
    object Emergency : ExpenseCategory("EMERGENCY", "Khẩn cấp", Icons.Default.Error, CategoryType.FINANCIALS)
    object Investment : ExpenseCategory("INVESTMENT", "Đầu tư", Icons.AutoMirrored.Filled.TrendingUp, CategoryType.FINANCIALS)
    object Others : ExpenseCategory("OTHERS", "Khác", Icons.Default.Category, CategoryType.FINANCIALS)

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
