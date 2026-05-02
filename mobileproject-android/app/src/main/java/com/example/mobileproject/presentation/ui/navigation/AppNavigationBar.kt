package com.example.mobileproject.presentation.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 compliant navigation bar
 * Provides smooth animations and consistent styling across the app
 */
@Composable
fun AppNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavigationItem> = NavigationConfig.navigationItems,
) {
    val colorScheme = MaterialTheme.colorScheme

    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = colorScheme.surface.copy(alpha = 0.98f),
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                NavigationBarItemContent(
                    item = item,
                    isSelected = currentRoute == item.route,
                    onNavigate = { onNavigate(item.route) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun NavigationBarItemContent(
    item: NavigationItem,
    isSelected: Boolean,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    
    // Smooth color animation for selected/unselected state
    val iconColor: Color by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
        label = "NavigationItemIconColor"
    )

    // Smooth background animation
    val backgroundColor: Color by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.9f) else Color.Transparent,
        label = "NavigationItemBackgroundColor"
    )

    Box(
        modifier = modifier
            .height(42.dp)
            .animateContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = backgroundColor,
            tonalElevation = 0.dp,
            modifier = Modifier
                .size(42.dp),
        ) {
            IconButton(
                onClick = onNavigate,
                modifier = Modifier.size(42.dp),
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = stringResource(item.contentDescriptionRes),
                    tint = iconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}
