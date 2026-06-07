package com.example.mobileproject.presentation.ui.screen.wallet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.presentation.ui.components.core.AppFullScreenLoading
import com.example.mobileproject.presentation.ui.screen.home.components.GoalCard
import com.example.mobileproject.presentation.viewmodel.SavingGoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingGoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SavingGoalViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.ensureLoaded()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mục tiêu tiết kiệm", fontWeight = FontWeight.Bold, color = colorScheme.onSurface) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại", tint = colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorScheme.surface,
                    titleContentColor = colorScheme.onSurface
                )
            )
        },
        containerColor = colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.goals.isEmpty()) {
            AppFullScreenLoading(message = "Đang tải mục tiêu tiết kiệm...")
        } else {
            val sortedGoals = uiState.goals.sortedWith(
                compareBy<SavingGoal> { it.status != GoalStatus.IN_PROGRESS }
                    .thenBy { it.deadline ?: "9999-99-99" }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sortedGoals) { goal ->
                    GoalCard(goal = goal)
                }
            }
        }
    }
}

