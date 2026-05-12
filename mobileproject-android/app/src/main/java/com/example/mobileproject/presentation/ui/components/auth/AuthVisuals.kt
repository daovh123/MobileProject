package com.example.mobileproject.presentation.ui.components.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.theme.AppTheme
import com.example.mobileproject.presentation.ui.components.core.AuthScreenStickers

@Composable
fun AuthBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = AppTheme.extendedColors

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Background image
        Image(
            painter = painterResource(R.drawable.bg_auth_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )

        // Dark scrim overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.70f),
                        ),
                    ),
                ),
        )

        // Decorative blur circles
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-96).dp)
                .size(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.28f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-96).dp, y = 132.dp)
                .size(300.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.secondary.copy(alpha = 0.22f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
        // Cute stickers
        AuthScreenStickers()

        content()
    }
}

@Composable
fun AuthBrandMark(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = AppTheme.extendedColors

    val pulseTransition = rememberInfiniteTransition(label = "brand-pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "brand-scale",
    )

    Surface(
        modifier = modifier
            .size(120.dp)
            .scale(pulseScale),
        shape = CircleShape,
        color = colorScheme.primaryContainer.copy(alpha = 0.92f),
        border = BorderStroke(2.dp, colorScheme.primary.copy(alpha = 0.35f)),
        shadowElevation = 24.dp,
        tonalElevation = 12.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "❤",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = colorScheme.primary,
            )
        }
    }
}

@Composable
fun AuthFormSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val extendedColors = AppTheme.extendedColors

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = extendedColors.glassBackground,
        border = BorderStroke(1.dp, extendedColors.glassBorder),
        shadowElevation = 16.dp,
        tonalElevation = 8.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            content = content,
        )
    }
}
