package com.example.mobileproject.presentation.ui.screen.memories

import androidx.compose.runtime.Composable

@Composable
fun MemoriesScreen(
    accessToken: String,
    onNavigateToExplore: () -> Unit,
) {
    LocketMemoriesScreen(
        accessToken = accessToken,
        onOpenCapture = onNavigateToExplore,
    )
}
