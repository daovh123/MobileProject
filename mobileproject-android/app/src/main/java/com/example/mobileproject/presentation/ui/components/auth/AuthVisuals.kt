package com.example.mobileproject.presentation.ui.components.auth

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val backgroundBrush = remember(colorScheme) {
        Brush.linearGradient(
            colors = listOf(
                colorScheme.surface,
                colorScheme.surfaceContainerLow.copy(alpha = 0.92f),
                colorScheme.surfaceVariant.copy(alpha = 0.92f),
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundBrush),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-96).dp)
                .size(260.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.primary.copy(alpha = 0.22f),
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
                            colorScheme.secondary.copy(alpha = 0.18f),
                            Color.Transparent,
                        ),
                    ),
                    shape = CircleShape,
                ),
        )

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = (-24).dp, y = 72.dp)
                .size(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            colorScheme.onSurfaceVariant.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
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

    Surface(
        modifier = modifier.size(104.dp),
        shape = CircleShape,
        color = colorScheme.primaryContainer.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.22f)),
        shadowElevation = 16.dp,
        tonalElevation = 10.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "❤",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onPrimaryContainer,
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

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colorScheme.surface.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.18f)),
        shadowElevation = 12.dp,
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            content = content,
        )
    }
}
