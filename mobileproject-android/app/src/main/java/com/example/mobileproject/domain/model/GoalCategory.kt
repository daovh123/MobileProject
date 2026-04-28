package com.example.mobileproject.domain.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class GoalCategory(
    val id: String,
    val displayName: String,
    val icon: ImageVector
) {
    object Travel : GoalCategory("TRAVEL", "Travel", Icons.Default.Flight)
    object Tech : GoalCategory("TECH", "Tech", Icons.Default.Devices)
    object Home : GoalCategory("HOME", "Home", Icons.Default.Home)
    object Savings : GoalCategory("SAVINGS", "Savings", Icons.Default.Savings)
    object Others : GoalCategory("OTHERS", "Others", Icons.Default.Star)

    companion object {
        fun getAll(): List<GoalCategory> = listOf(
            Travel, Tech, Home, Savings, Others
        )
        
        fun fromId(id: String): GoalCategory = getAll().find { it.id == id } ?: Others
    }
}
