package com.example.mobileproject.presentation.ui.components.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text

@Composable
fun AutoShrinkSingleLineText(
    text: String,
    maxFontSize: TextUnit,
    modifier: Modifier = Modifier,
    minFontSize: TextUnit = 12.sp,
    stepGranularity: TextUnit = 1.sp,
    style: TextStyle = LocalTextStyle.current,
    color: Color = Color.Unspecified,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    var currentFontSize by remember(text, maxFontSize, minFontSize, stepGranularity) {
        mutableStateOf(maxFontSize)
    }
    var readyToDraw by remember(text, maxFontSize, minFontSize, stepGranularity) {
        mutableStateOf(false)
    }

    Box(
        modifier = modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            style = style.copy(fontSize = currentFontSize),
            color = color,
            fontWeight = fontWeight,
            textAlign = textAlign,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                if (result.hasVisualOverflow && currentFontSize > minFontSize) {
                    val nextFontSize = (currentFontSize.value - stepGranularity.value)
                        .coerceAtLeast(minFontSize.value)
                        .sp
                    if (nextFontSize != currentFontSize) {
                        currentFontSize = nextFontSize
                        readyToDraw = false
                    } else {
                        readyToDraw = true
                    }
                } else {
                    readyToDraw = true
                }
            },
        )
    }
}
