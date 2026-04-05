package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.mobileproject.R
import com.example.mobileproject.presentation.seed.SeedDataProvider

@Composable
fun WalletScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.md3_surface_variant))
            .padding(20.dp),
    ) {
        Text(
            text = stringResource(R.string.page_wallet),
            style = MaterialTheme.typography.headlineMedium,
            color = colorResource(R.color.md3_primary),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.md3_surface)),
            shape = MaterialTheme.shapes.large,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = SeedDataProvider.walletActivities.getOrNull(0).orEmpty(),
                    color = colorResource(R.color.md3_on_surface),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SeedDataProvider.walletActivities.getOrNull(1).orEmpty(),
                    color = colorResource(R.color.md3_on_surface_variant),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = SeedDataProvider.walletActivities.getOrNull(2).orEmpty(),
                    color = colorResource(R.color.md3_on_surface_variant),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
