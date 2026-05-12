package com.example.mobileproject.presentation.ui.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 transparent navigation bar with animated indicator and labels.
 * No background container — floats over the screen content.
 */
@Composable
fun AppNavigationBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavigationItem> = NavigationConfig.navigationItems,
) {
    val colorScheme = MaterialTheme.colorScheme

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
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

@Composable
private fun NavigationBarItemContent(
    item: NavigationItem,
    isSelected: Boolean,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme

    val iconColor: Color by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
        label = "NavigationItemIconColor",
    )

    val backgroundColor: Color by animateColorAsState(
        targetValue = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.85f) else Color.Transparent,
        label = "NavigationItemBackgroundColor",
    )

    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 56.dp else 42.dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "NavigationItemWidth",
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = backgroundColor,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .width(indicatorWidth)
                    .height(36.dp),
            ) {
                IconButton(
                    onClick = onNavigate,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.contentDescriptionRes),
                        tint = iconColor,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            if (isSelected) {
                Text(
                    text = stringResource(item.labelRes),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.primary,
                )
            }
        }
    }
}
