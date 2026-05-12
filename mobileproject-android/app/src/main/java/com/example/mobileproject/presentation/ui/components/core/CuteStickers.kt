package com.example.mobileproject.presentation.ui.components.core

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Cute decorative emoji stickers for the couple/love-themed app.
 * These float around screens with subtle animations.
 */

// ─── Individual Animated Sticker ─────────────────────────────────────
@Composable
fun FloatingSticker(
    emoji: String,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    alpha: Float = 0.55f,
    animDuration: Int = 2400,
    rotationRange: Float = 8f,
    scaleRange: Float = 0.08f,
) {
    val transition = rememberInfiniteTransition(label = "sticker-$emoji")

    val rotation by transition.animateFloat(
        initialValue = -rotationRange,
        targetValue = rotationRange,
        animationSpec = infiniteRepeatable(
            animation = tween(animDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sticker-rot-$emoji",
    )

    val scale by transition.animateFloat(
        initialValue = 1f - scaleRange,
        targetValue = 1f + scaleRange,
        animationSpec = infiniteRepeatable(
            animation = tween((animDuration * 0.8f).toInt()),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "sticker-scale-$emoji",
    )

    Text(
        text = emoji,
        fontSize = (size.value).sp,
        modifier = modifier
            .alpha(alpha)
            .rotate(rotation)
            .scale(scale),
    )
}

// ─── Preset Sticker Sets ─────────────────────────────────────────────

/** Love-themed stickers for the home dashboard */
@Composable
fun BoxScope.HomeLoveStickers() {
    FloatingSticker(
        emoji = "💕",
        modifier = Modifier.align(Alignment.TopStart).offset(x = 12.dp, y = 8.dp),
        size = 22.dp,
        alpha = 0.45f,
        animDuration = 2600,
    )
    FloatingSticker(
        emoji = "✨",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = 14.dp),
        size = 18.dp,
        alpha = 0.40f,
        animDuration = 1800,
    )
    FloatingSticker(
        emoji = "🌸",
        modifier = Modifier.align(Alignment.BottomStart).offset(x = 20.dp, y = (-12).dp),
        size = 20.dp,
        alpha = 0.38f,
        animDuration = 3000,
    )
    FloatingSticker(
        emoji = "💗",
        modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-14).dp, y = (-16).dp),
        size = 20.dp,
        alpha = 0.42f,
        animDuration = 2200,
    )
}

/** Stickers around the "Days Together" card */
@Composable
fun BoxScope.DaysTogetherStickers() {
    FloatingSticker(
        emoji = "💑",
        modifier = Modifier.align(Alignment.TopStart).offset(x = 14.dp, y = 10.dp),
        size = 24.dp,
        alpha = 0.50f,
        animDuration = 2800,
    )
    FloatingSticker(
        emoji = "🦋",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-12).dp, y = 8.dp),
        size = 20.dp,
        alpha = 0.42f,
        animDuration = 2000,
    )
    FloatingSticker(
        emoji = "🌷",
        modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-16).dp, y = (-10).dp),
        size = 20.dp,
        alpha = 0.40f,
        animDuration = 2500,
    )
}

/** Stickers for the balance/wallet card */
@Composable
fun BoxScope.BalanceCardStickers() {
    FloatingSticker(
        emoji = "💰",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = 8.dp),
        size = 22.dp,
        alpha = 0.38f,
        animDuration = 2600,
    )
    FloatingSticker(
        emoji = "🌟",
        modifier = Modifier.align(Alignment.BottomStart).offset(x = 10.dp, y = (-8).dp),
        size = 16.dp,
        alpha = 0.35f,
        animDuration = 1900,
    )
}

/** Romantic stickers for couple connect screen */
@Composable
fun BoxScope.CoupleConnectStickers() {
    FloatingSticker(
        emoji = "💘",
        modifier = Modifier.align(Alignment.TopStart).offset(x = 24.dp, y = 60.dp),
        size = 28.dp,
        alpha = 0.50f,
        animDuration = 2400,
    )
    FloatingSticker(
        emoji = "🌹",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-20).dp, y = 80.dp),
        size = 24.dp,
        alpha = 0.45f,
        animDuration = 3200,
    )
    FloatingSticker(
        emoji = "💫",
        modifier = Modifier.align(Alignment.CenterStart).offset(x = 16.dp),
        size = 22.dp,
        alpha = 0.40f,
        animDuration = 2000,
    )
    FloatingSticker(
        emoji = "🦢",
        modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-18).dp),
        size = 26.dp,
        alpha = 0.42f,
        animDuration = 2800,
    )
    FloatingSticker(
        emoji = "🌸",
        modifier = Modifier.align(Alignment.BottomStart).offset(x = 30.dp, y = (-80).dp),
        size = 22.dp,
        alpha = 0.38f,
        animDuration = 2600,
    )
    FloatingSticker(
        emoji = "💝",
        modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-24).dp, y = (-100).dp),
        size = 24.dp,
        alpha = 0.44f,
        animDuration = 2200,
    )
}

/** Profile header stickers */
@Composable
fun BoxScope.ProfileHeaderStickers() {
    FloatingSticker(
        emoji = "👑",
        modifier = Modifier.align(Alignment.TopStart).offset(x = 16.dp, y = 8.dp),
        size = 20.dp,
        alpha = 0.42f,
        animDuration = 2400,
    )
    FloatingSticker(
        emoji = "💎",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-14).dp, y = 10.dp),
        size = 18.dp,
        alpha = 0.38f,
        animDuration = 2800,
    )
    FloatingSticker(
        emoji = "🌺",
        modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-18).dp, y = (-6).dp),
        size = 18.dp,
        alpha = 0.36f,
        animDuration = 2200,
    )
}

/** Auth screen (login/register) stickers */
@Composable
fun BoxScope.AuthScreenStickers() {
    FloatingSticker(
        emoji = "💕",
        modifier = Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = 50.dp),
        size = 26.dp,
        alpha = 0.50f,
        animDuration = 2600,
    )
    FloatingSticker(
        emoji = "🌙",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-18).dp, y = 70.dp),
        size = 22.dp,
        alpha = 0.44f,
        animDuration = 3000,
    )
    FloatingSticker(
        emoji = "✨",
        modifier = Modifier.align(Alignment.CenterStart).offset(x = 14.dp, y = (-40).dp),
        size = 18.dp,
        alpha = 0.38f,
        animDuration = 1800,
    )
    FloatingSticker(
        emoji = "🌸",
        modifier = Modifier.align(Alignment.CenterEnd).offset(x = (-16).dp, y = 30.dp),
        size = 20.dp,
        alpha = 0.40f,
        animDuration = 2400,
    )
    FloatingSticker(
        emoji = "💗",
        modifier = Modifier.align(Alignment.BottomStart).offset(x = 24.dp, y = (-60).dp),
        size = 22.dp,
        alpha = 0.42f,
        animDuration = 2200,
    )
}

/** Memories / Calendar stickers */
@Composable
fun BoxScope.MemoriesStickers() {
    FloatingSticker(
        emoji = "📸",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-12).dp, y = 6.dp),
        size = 18.dp,
        alpha = 0.35f,
        animDuration = 2400,
    )
    FloatingSticker(
        emoji = "🎀",
        modifier = Modifier.align(Alignment.TopStart).offset(x = 10.dp, y = 4.dp),
        size = 16.dp,
        alpha = 0.32f,
        animDuration = 2800,
    )
}

/** Wallet screen stickers */
@Composable
fun BoxScope.WalletStickers() {
    FloatingSticker(
        emoji = "🐷",
        modifier = Modifier.align(Alignment.TopEnd).offset(x = (-16).dp, y = 10.dp),
        size = 22.dp,
        alpha = 0.40f,
        animDuration = 2600,
    )
    FloatingSticker(
        emoji = "💰",
        modifier = Modifier.align(Alignment.TopStart).offset(x = 14.dp, y = 8.dp),
        size = 18.dp,
        alpha = 0.35f,
        animDuration = 2200,
    )
}
