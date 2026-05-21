package com.example.mobileproject.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class GoalCategory(
    val id: String,
    val displayName: String,
    val icon: ImageVector
) {
    object Travel : GoalCategory("TRAVEL", "Du lịch", Icons.Default.Flight)
    object Tech : GoalCategory("TECH", "Công nghệ", Icons.Default.Devices)
    object Home : GoalCategory("HOME", "Nhà cửa", Icons.Default.Home)
    object Savings : GoalCategory("TIẾT KIỆM", "Tiết kiệm", Icons.Default.Savings)
    object Others : GoalCategory("OTHERS", "Khác", Icons.Default.Star)

    companion object {
        fun getAll(): List<GoalCategory> = listOf(
            Travel, Tech, Home, Savings, Others
        )
        
        fun fromId(id: String): GoalCategory = getAll().find { it.id == id } ?: Others
    }
}

