/**
 * Material Icons cho bottom navigation bar.
 *
 * Cung cấp object [MaterialIcons] với các icon Filled/Rounded
 * cho 5 tab chính: Home, Explore, Wallet, Heart, Profile.
 */
package com.example.mobileproject.presentation.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icon cho thanh điều hướng dưới cùng (bottom navigation bar).
 *
 * Sử dụng Material Icons Filled cho các tab chính và Rounded cho Explore,
 * đảm bảo tính nhất quán với Material Design 3.
 *
 * Phân loại:
 * - [Home]: Tab Trang chủ (Icons.Filled.Home).
 * - [Explore]: Tab Khám phá (Icons.Rounded.Explore).
 * - [Wallet]: Tab Ví (Icons.Filled.Wallet).
 * - [Heart]: Tab Kỷ niệm (Icons.Filled.Favorite).
 * - [Profile]: Tab Hồ sơ (Icons.Filled.Person).
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
