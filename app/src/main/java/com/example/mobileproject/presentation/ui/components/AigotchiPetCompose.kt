package com.example.mobileproject.presentation.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

val BrandOrange = Color(0xFFFDBA74)
val DarkOrange = Color(0xFFEA580C)
val BellyColor = Color(0xFFFFF5E1).copy(alpha = 0.6f)
val BlushColor = Color(0xFFFDA4AF).copy(alpha = 0.5f)
val EyeColor = Color(0xFF1E293B)
val CollarColor = Color(0xFF334155)
val TechBlue = Color(0xFF22D3EE)

@Composable
fun AigotchiPetCompose(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "AigotchiAnimations")

    val breathing by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Breathing",
    )

    val blinkScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3800
                0.1f at 3900
                1f at 4000
            },
            repeatMode = RepeatMode.Restart,
        ),
        label = "Blinking",
    )

    val tailWag by transition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "TailWag",
    )

    Box(
        modifier = modifier.size(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2

            val tailPath = Path().apply {
                moveTo(centerX + 50f, centerY + 40f)
                quadraticBezierTo(
                    centerX + 80f + tailWag,
                    centerY + 40f,
                    centerX + 70f + tailWag,
                    centerY,
                )
            }
            drawPath(
                path = tailPath,
                color = BrandOrange,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round),
            )

            val bodyHeight = 50.dp.toPx() + (breathing * 3.dp.toPx())
            drawOval(
                color = BrandOrange,
                topLeft = Offset(centerX - 65.dp.toPx(), centerY + 40.dp.toPx() - bodyHeight),
                size = Size(130.dp.toPx(), bodyHeight * 2),
            )

            drawOval(
                color = BellyColor,
                topLeft = Offset(centerX - 40.dp.toPx(), centerY + 50.dp.toPx() - 30.dp.toPx()),
                size = Size(80.dp.toPx(), 60.dp.toPx()),
            )

            drawCircle(
                color = BrandOrange,
                radius = 12.dp.toPx(),
                center = Offset(centerX - 35.dp.toPx(), centerY + 80.dp.toPx()),
            )
            drawCircle(
                color = BrandOrange,
                radius = 12.dp.toPx(),
                center = Offset(centerX + 35.dp.toPx(), centerY + 80.dp.toPx()),
            )

            val headOffset = -4.dp.toPx() * breathing
            val headCenterY = centerY - 15.dp.toPx() + headOffset

            val leftEar = Path().apply {
                moveTo(centerX - 35.dp.toPx(), headCenterY - 15.dp.toPx())
                lineTo(centerX - 55.dp.toPx(), headCenterY - 55.dp.toPx())
                lineTo(centerX - 10.dp.toPx(), headCenterY - 30.dp.toPx())
                close()
            }
            drawPath(path = leftEar, color = BrandOrange)

            val rightEar = Path().apply {
                moveTo(centerX + 35.dp.toPx(), headCenterY - 15.dp.toPx())
                lineTo(centerX + 55.dp.toPx(), headCenterY - 55.dp.toPx())
                lineTo(centerX + 10.dp.toPx(), headCenterY - 30.dp.toPx())
                close()
            }
            drawPath(path = rightEar, color = BrandOrange)

            drawCircle(
                color = BrandOrange,
                radius = 55.dp.toPx(),
                center = Offset(centerX, headCenterY),
            )

            drawCircle(
                color = BlushColor,
                radius = 7.dp.toPx(),
                center = Offset(centerX - 40.dp.toPx(), headCenterY + 15.dp.toPx()),
            )
            drawCircle(
                color = BlushColor,
                radius = 7.dp.toPx(),
                center = Offset(centerX + 40.dp.toPx(), headCenterY + 15.dp.toPx()),
            )

            val eyeRadius = 10.dp.toPx()
            val eyeY = headCenterY

            drawOval(
                color = EyeColor,
                topLeft = Offset(centerX - 25.dp.toPx() - eyeRadius, eyeY - (eyeRadius * blinkScale)),
                size = Size(eyeRadius * 2, eyeRadius * 2 * blinkScale),
            )
            drawOval(
                color = EyeColor,
                topLeft = Offset(centerX + 25.dp.toPx() - eyeRadius, eyeY - (eyeRadius * blinkScale)),
                size = Size(eyeRadius * 2, eyeRadius * 2 * blinkScale),
            )

            val mouthPath = Path().apply {
                moveTo(centerX - 8.dp.toPx(), headCenterY + 20.dp.toPx())
                quadraticBezierTo(
                    centerX,
                    headCenterY + 30.dp.toPx(),
                    centerX + 8.dp.toPx(),
                    headCenterY + 20.dp.toPx(),
                )
            }
            drawPath(
                path = mouthPath,
                color = DarkOrange,
                style = Stroke(width = 2.dp.toPx()),
            )

            drawRoundRect(
                color = CollarColor,
                topLeft = Offset(centerX - 25.dp.toPx(), centerY + 25.dp.toPx()),
                size = Size(50.dp.toPx(), 6.dp.toPx()),
                cornerRadius = CornerRadius(3.dp.toPx()),
            )

            drawCircle(
                color = TechBlue,
                radius = 5.dp.toPx(),
                center = Offset(centerX, centerY + 32.dp.toPx()),
                alpha = 0.4f + (breathing * 0.6f),
            )
        }
    }
}
