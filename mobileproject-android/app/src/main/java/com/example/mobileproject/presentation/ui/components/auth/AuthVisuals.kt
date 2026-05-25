package com.example.mobileproject.presentation.ui.components.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AuthBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val isDark = colorScheme.background.red < 0.5f
    val backdropColors = if (isDark) {
        listOf(
            Color(0xFF1A1614),
            Color(0xFF221E1C),
            Color(0xFF14110F),
        )
    } else {
        listOf(
            Color(0xFFFFFBFA),
            Color(0xFFFFF1F3),
            Color(0xFFFFE8EC),
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = backdropColors,
                    ),
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 84.dp, y = (-96).dp)
                .size(240.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(colorScheme.primary.copy(alpha = 0.1f), Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = (-88).dp, y = 120.dp)
                .size(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(colorScheme.secondary.copy(alpha = 0.09f), Color.Transparent),
                    ),
                    shape = CircleShape,
                ),
        )

        content()
    }
}

@Composable
fun AuthBrandMark(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme

    val pulseTransition = rememberInfiniteTransition(label = "brand-pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "brand-scale",
    )

    Surface(
        modifier = modifier
            .size(96.dp)
            .scale(pulseScale),
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.9f),
        border = BorderStroke(1.5.dp, colorScheme.primary.copy(alpha = 0.28f)),
        shadowElevation = 12.dp,
        tonalElevation = 2.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "❤",
                style = MaterialTheme.typography.headlineLarge,
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
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color.White.copy(alpha = 0.97f),
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            content = content,
        )
    }
}
