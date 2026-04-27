package com.example.mobileproject.presentation.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Material Icons with modern styling
 * Provides a clean, modern look similar to Material Design
 */

object MaterialIcons {
    val Home: ImageVector
        get() = Icons.Filled.Home

    val Explore: ImageVector
        get() = Icons.Rounded.Explore

    val Wallet: ImageVector
        get() = Icons.Filled.Wallet

    val Heart: ImageVector
        get() = Icons.Filled.Favorite

    val Profile: ImageVector
        get() = Icons.Filled.Person
}
