/**
 * # FutureGoalsScreen - Màn hình mục tiêu tương lai
 *
 * Hiển thị danh sách mục tiêu tương lai (FutureGoal) của người dùng.
 * Khác với SavingGoalsScreen, màn hình này hiển thị FutureGoal có hỗ trợ
 * đánh dấu hoàn thành task con (onTaskToggle).
 *
 * ## ViewModel bindings
 * - [GoalViewModel]: tải mục tiêu, toggle task con trong mục tiêu
 *
 * ## Layout
 * - Scaffold với TopAppBar "Mục tiêu tương lai"
 * - LazyColumn hiển thị GoalCard với callback onTaskToggle
 * - Sắp xếp: chưa hoàn thành lên trước, theo deadline
 *
 * ## Navigation triggers
 * - onNavigateBack: quay lại màn hình trước
 */
package com.example.mobileproject.presentation.ui.screen.wallet

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.domain.entity.FutureGoal
import com.example.mobileproject.domain.entity.GoalStatus
import com.example.mobileproject.presentation.ui.screen.home.components.GoalCard
import com.example.mobileproject.presentation.viewmodel.GoalViewModel

/**
 * Màn hình mục tiêu tương lai.
 * Hiển thị FutureGoal với khả năng đánh dấu task con hoàn thành.
 *
 * @param onNavigateBack quay lại
 * @param viewModel GoalViewModel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FutureGoalsScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
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
                title = { Text("Mục tiêu tương lai", fontWeight = FontWeight.Bold, color = colorScheme.onSurface) },
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
        val futureGoals = uiState.goals.filterIsInstance<FutureGoal>()
        if (uiState.isLoading && futureGoals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            val sortedGoals = futureGoals.sortedWith(
                compareBy<FutureGoal> { it.status == GoalStatus.ACHIEVED }
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
                    GoalCard(
                        goal = goal,
                        onTaskToggle = { goalId, taskId ->
                            viewModel.toggleTask(goalId, taskId)
                        }
                    )
                }
            }
        }
    }
}

