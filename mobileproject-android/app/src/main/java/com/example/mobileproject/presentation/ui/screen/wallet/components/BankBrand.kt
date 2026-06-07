package com.example.mobileproject.presentation.ui.screen.wallet.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mobileproject.presentation.model.wallet.VietnamBank

@Composable
fun BankLogo(
    bank: VietnamBank?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    if (bank == null) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "B",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
            )
        }
        return
    }

    Image(
        painter = painterResource(id = bank.logoRes),
        contentDescription = bank.name,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape((size.value / 4f).dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
        contentScale = ContentScale.Crop,
    )
}

@Composable
fun BankNameWithLogo(
    bank: VietnamBank?,
    modifier: Modifier = Modifier,
    logoSize: Dp = 36.dp,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        BankLogo(bank = bank, size = logoSize)
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = bank?.name ?: "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
