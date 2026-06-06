/**
 * # SavingGoalsScreen - Màn hình danh sách mục tiêu tiết kiệm
 *
 * Hiển thị tất cả mục tiêu tiết kiệm (SavingGoal) của người dùng.
 * Danh sách được sắp xếp: mục tiêu chưa hoàn thành lên trước, sau đó theo deadline gần nhất.
 *
 * ## ViewModel bindings
 * - [SavingGoalViewModel]: tải danh sách mục tiêu qua loadGoals()
 *
 * ## Layout
 * - Scaffold với TopAppBar "Mục tiêu tiết kiệm"
 * - LazyColumn hiển thị GoalCard cho từng mục tiêu
 * - Loading state: CircularProgressIndicator khi đang tải lần đầu
 *
 * ## Navigation triggers
 * - onNavigateBack: quay lại màn hình trước
 */
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.presentation.ui.screen.home.components.GoalCard
import com.example.mobileproject.presentation.viewmodel.SavingGoalViewModel

/**
 * Màn hình danh sách mục tiêu tiết kiệm.
 * Hiển thị tất cả SavingGoal, sắp xếp: chưa hoàn thành trước, theo deadline.
 *
 * @param onNavigateBack quay lại
 * @param viewModel SavingGoalViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingGoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SavingGoalViewModel = hiltViewModel()
) {
    val colorScheme = MaterialTheme.colorScheme
    val uiState by viewModel.uiState.collectAsState()

    // Tải danh sách mục tiêu khi composable mount
    LaunchedEffect(Unit) {
        viewModel.loadGoals()
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            val sortedGoals = uiState.goals.sortedWith(
                compareBy<SavingGoal> { it.status == GoalStatus.ACHIEVED }
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

