package com.example.mobileproject.presentation.ui.screen.memories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.presentation.ui.components.place.PlaceCard
import com.example.mobileproject.presentation.viewmodel.FavoriteViewModel
import com.example.mobileproject.presentation.viewmodel.HistoryViewModel

@Composable
fun MemoriesScreen(
    accessToken: String,
    onNavigateToExplore: () -> Unit,
) {
    val favoriteViewModel: FavoriteViewModel = hiltViewModel()
    val historyViewModel: HistoryViewModel = hiltViewModel()

    val favoriteState by favoriteViewModel.uiState.collectAsState()
    val historyState by historyViewModel.uiState.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(accessToken) {
        favoriteViewModel.loadFavorites(accessToken)
        historyViewModel.loadHistory(accessToken)
    }

    val isFavoritesTab = selectedTab == 0
    val places = if (isFavoritesTab) favoriteState.favorites else historyState.history
    val isLoading = if (isFavoritesTab) favoriteState.isLoading else historyState.isLoading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.md3_surface_variant))
            .padding(16.dp),
    ) {
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colorResource(R.color.md3_surface),
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(stringResource(R.string.memories_tab_favorites)) },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(stringResource(R.string.memories_tab_history)) },
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                places.isEmpty() -> {
                    Text(
                        text = stringResource(R.string.memories_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorResource(R.color.md3_on_surface_variant),
                        modifier = Modifier.align(Alignment.Center),
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(items = places, key = { it.id }) { place ->
                            PlaceCard(
                                place = place,
                                modifier = Modifier.padding(bottom = 16.dp),
                                onClick = { onNavigateToExplore() },
                            )
                        }
                    }
                }
            }
        }

        if (!isFavoritesTab && !isLoading && places.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            TextButton(
                onClick = { historyViewModel.clearHistory(accessToken) },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(text = stringResource(R.string.memories_clear_history))
            }
        }
    }
}
