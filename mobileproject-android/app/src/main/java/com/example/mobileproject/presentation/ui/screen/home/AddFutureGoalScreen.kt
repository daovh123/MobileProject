/**
 * AddFutureGoalScreen - Màn hình tạo mục tiêu tương lai mới.
 *
 * Mục đích:
 * - Cho phép người dùng tạo mục tiêu tương lai với tên, danh sách tasks, danh mục, ngày hết hạn.
 * - Khác với SavingGoal: không có số tiền mục tiêu, thay vào đó là danh sách công việc cần hoàn thành.
 *
 * Layout:
 * - Scaffold với TopAppBar (nút back + tiêu đề).
 * - Column cuộn dọc chứa: tên, tasks (danh sách động), danh mục, ngày hết hạn.
 * - Tasks có thể thêm/xóa động, mỗi task có nút xóa (hiện khi > 1 task).
 *
 * ViewModel: [GoalViewModel] - quan sát createSuccess và error.
 *
 * Navigation:
 * - Tạo thành công → quay lại màn hình trước (onNavigateBack).
 *
 * Được host bởi: [HomeActivity] (qua navigation graph).
 */
package com.example.mobileproject.presentation.ui.screen.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.GoalTask
import com.example.mobileproject.domain.model.ExpenseCategory
import com.example.mobileproject.presentation.ui.screen.add_expense.components.CategoryBottomSheet
import com.example.mobileproject.presentation.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

/**
 * AddFutureGoalScreen - Màn hình tạo mục tiêu tương lai mới.
 *
 * Mục đích:
 * - Cho phép người dùng tạo mục tiêu tương lai với tên, danh sách tasks, danh mục, ngày hết hạn.
 * - Khác với SavingGoal: không có số tiền mục tiêu, thay vào đó là danh sách công việc cần hoàn thành.
 *
 * Layout:
 * - Scaffold với TopAppBar (nút back + tiêu đề).
 * - Column cuộn dọc chứa: tên, tasks (danh sách động), danh mục, ngày hết hạn.
 * - Tasks có thể thêm/xóa động, mỗi task có nút xóa (hiện khi > 1 task).
 *
 * ViewModel: [GoalViewModel] - quan sát createSuccess và error.
 *
 * Navigation:
 * - Tạo thành công → quay lại màn hình trước (onNavigateBack).
 *
 * Được host bởi: [HomeActivity] (qua navigation graph).
 *
 * @param onNavigateBack Callback quay lại màn hình trước.
 * @param viewModel GoalViewModel xử lý logic tạo mục tiêu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFutureGoalScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme

    var name by remember { mutableStateOf("") }
    // Danh sách tasks: bắt đầu với 1 task rỗng, có thể thêm/xóa động
    var tasks by remember { mutableStateOf(listOf("")) }
    // Danh mục mặc định: Education
    var selectedCategory by remember { mutableStateOf<ExpenseCategory>(ExpenseCategory.Education) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    val datePickerState = rememberDatePickerState()

    // LaunchedEffect: theo dõi createSuccess. Khi tạo thành công → quay lại + clear message.
    LaunchedEffect(state.createSuccess) {
        if (state.createSuccess) {
            onNavigateBack()
            viewModel.clearMessage()
        }
    }

    // LaunchedEffect: theo dõi error. Khi có lỗi → hiện Toast + clear message.
    LaunchedEffect(state.error) {
        state.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.common_ok), color = colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showCategorySheet) {
        CategoryBottomSheet(
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it },
            onDismiss = { showCategorySheet = false },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.goal_new_future_title),
                        color = colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.common_back),
                            tint = colorScheme.primary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surface),
            )
        },
        containerColor = colorScheme.surface,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.goal_name_label), fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                TextField(
                    value = name,
                    onValueChange = { if (it.length <= 50) name = it },
                    placeholder = {
                        Text(
                            stringResource(R.string.goal_name_placeholder_future),
                            color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = colorScheme.surfaceContainerLowest,
                        focusedIndicatorColor = colorScheme.primary,
                        unfocusedIndicatorColor = colorScheme.outlineVariant,
                    ),
                    shape = RoundedCornerShape(16.dp),
                    supportingText = {
                        Text(
                            text = "${name.length}/50",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                        )
                    },
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.goal_tasks_label), fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                tasks.forEachIndexed { index, task ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TextField(
                            value = task,
                            onValueChange = { newText ->
                                tasks = tasks.toMutableList().apply { this[index] = newText }
                            },
                            placeholder = {
                                Text(
                                    stringResource(R.string.goal_task_placeholder),
                                    color = colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = colorScheme.surfaceContainerLowest,
                                unfocusedContainerColor = colorScheme.surfaceContainerLowest,
                                focusedIndicatorColor = colorScheme.primary,
                                unfocusedIndicatorColor = colorScheme.outlineVariant,
                            ),
                            shape = RoundedCornerShape(16.dp),
                        )
                        if (tasks.size > 1) {
                            IconButton(onClick = { tasks = tasks.toMutableList().apply { removeAt(index) } }) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = colorScheme.primary)
                            }
                        }
                    }
                }

                TextButton(
                    onClick = { tasks = tasks + "" },
                    modifier = Modifier.align(Alignment.Start),
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.primary),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.goal_add_task), fontWeight = FontWeight.Bold)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.goal_category_label), fontWeight = FontWeight.Bold, color = colorScheme.onSurface)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    val quickCategories = listOf(ExpenseCategory.Travel, ExpenseCategory.Education, ExpenseCategory.Household)
                    quickCategories.forEach { cat ->
                        FutureCategoryCircle(
                            category = cat,
                            isSelected = selectedCategory.id == cat.id,
                            onClick = { selectedCategory = cat },
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surfaceContainerLowest)
                                .border(1.dp, colorScheme.outlineVariant, CircleShape)
                                .clickable { showCategorySheet = true },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.goal_more_label),
                                tint = colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp),
                            )
                        }
                        Text(
                            stringResource(R.string.goal_more_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(R.string.goal_target_date_required),
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                )
                Surface(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (selectedDateMillis == null) colorScheme.primary.copy(alpha = 0.5f) else colorScheme.outlineVariant,
                            shape = RoundedCornerShape(16.dp),
                        ),
                    shape = RoundedCornerShape(16.dp),
                    color = colorScheme.surfaceContainerLowest,
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = colorScheme.primary)
                        val dateText = selectedDateMillis?.let {
                            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
                        } ?: stringResource(R.string.goal_select_date)
                        Text(
                            text = dateText,
                            color = if (selectedDateMillis == null) colorScheme.onSurfaceVariant else colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isFormValid = name.isNotBlank() && tasks.any { it.isNotBlank() } && selectedDateMillis != null
            Button(
                onClick = {
                    if (isFormValid) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        val deadlineStr = sdf.format(Date(selectedDateMillis!!))
                        val goalTasks = tasks.filter { it.isNotBlank() }.map {
                            GoalTask(taskId = UUID.randomUUID().toString(), content = it, isCompleted = false)
                        }
                        viewModel.createFutureGoal(
                            name = name,
                            category = selectedCategory.id,
                            deadline = deadlineStr,
                            tasks = goalTasks,
                        )
                    } else {
                        Toast.makeText(context, "Please fill in all required fields (Name, Date, Task)", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !state.isCreating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    disabledContainerColor = colorScheme.primary.copy(alpha = 0.5f),
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = colorScheme.onPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null, tint = colorScheme.onPrimary)
                    }
                    Text(
                        if (state.isCreating) "Đang tạo..." else "Tạo Mục Tiêu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorScheme.onPrimary,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

/**
 * Composable hiển thị danh mục dưới dạng hình tròn với icon (cho FutureGoal).
 *
 * @param category Danh mục cần hiển thị.
 * @param isSelected Có đang được chọn không.
 * @param onClick Callback khi nhấn vào.
 */
@Composable
fun FutureCategoryCircle(
    category: ExpenseCategory,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceContainerLowest)
                .border(2.dp, if (isSelected) colorScheme.primary else colorScheme.outlineVariant, CircleShape)
                .clickable { onClick() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
        Text(
            text = category.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) colorScheme.primary else colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
