package com.example.mobileproject.presentation.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.icons.LucideBell
import com.example.mobileproject.presentation.ui.icons.LucideClose
import com.example.mobileproject.presentation.ui.icons.LucideCompass
import com.example.mobileproject.presentation.ui.icons.LucideHeart
import com.example.mobileproject.presentation.ui.icons.LucideHome
import com.example.mobileproject.presentation.ui.icons.LucideImage
import com.example.mobileproject.presentation.ui.icons.LucideLock
import com.example.mobileproject.presentation.ui.icons.LucideMail
import com.example.mobileproject.presentation.ui.icons.LucideMapPin
import com.example.mobileproject.presentation.ui.icons.LucidePercent
import com.example.mobileproject.presentation.ui.icons.LucideSearch
import com.example.mobileproject.presentation.ui.icons.LucideShield
import com.example.mobileproject.presentation.ui.icons.LucideShoppingCart
import com.example.mobileproject.presentation.ui.icons.LucideUser
import com.example.mobileproject.presentation.ui.icons.LucideWallet

/**
 * Centralized navigation configuration for Material Design 3 consistency
 * Ensures all navigation items are synchronized across the entire app
 */
data class NavigationItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val contentDescriptionRes: Int,
)

object NavigationConfig {
    val navigationItems = listOf(
        NavigationItem(
            route = "home",
            labelRes = R.string.page_home,
            icon = LucideHome,
            contentDescriptionRes = R.string.cd_home,
        ),
        NavigationItem(
            route = "explore",
            labelRes = R.string.nav_search,
            icon = LucideCompass,
            contentDescriptionRes = R.string.cd_explore,
        ),
        NavigationItem(
            route = "wallet",
            labelRes = R.string.page_wallet,
            icon = LucideWallet,
            contentDescriptionRes = R.string.cd_wallet,
        ),
        NavigationItem(
            route = "memories",
            labelRes = R.string.nav_memories,
            icon = LucideHeart,
            contentDescriptionRes = R.string.cd_memories,
        ),
        NavigationItem(
            route = "profile",
            labelRes = R.string.profile_title,
            icon = LucideUser,
            contentDescriptionRes = R.string.cd_profile,
        ),
    )

    fun getItemByRoute(route: String): NavigationItem? =
        navigationItems.firstOrNull { it.route == route }
}
