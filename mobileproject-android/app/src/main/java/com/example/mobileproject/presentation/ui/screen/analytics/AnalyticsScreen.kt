package com.example.mobileproject.presentation.ui.screen.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobileproject.R
import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.presentation.viewmodel.AnalyticsViewModel
import java.text.NumberFormat
import java.util.*

/**
 * Màn hình Thống kê chi tiêu (Analytics) – hiển thị biểu đồ phân bổ và xu hướng.
 *
 * Cấu trúc UI chính:
 * - [PieChart]: biểu đồ tròn (Canvas drawArc) hiển thị phân bổ chi tiêu theo danh mục.
 *   Chú thích (legend) bên phải hiển thị tên danh mục + phần trăm.
 * - [BarChart]: biểu đồ cột đôi (Canvas drawRect) so sánh thu nhập vs chi tiêu
 *   theo thời gian. Cột xanh = thu nhập, cột đỏ = chi tiêu.
 * - [AnalyticsShimmer]: placeholder loading dạng box xám khi dữ liệu đang tải.
 * - [ChartCard]: Card container cho mỗi biểu đồ, bo góc 16dp.
 *
 * Canvas drawing notes:
 * - PieChart: drawArc với useCenter=true, startAngle tích lũy qua mỗi phần.
 * - BarChart: tính barWidth = canvasWidth / (dataSize * 3), mỗi pair cách nhau
 *   1 barWidth spacing.
 *
 * ViewModel: [AnalyticsViewModel] (Hilt-injected) – loadAnalytics(coupleId, year, month).
 *
 * Layout: Scaffold + LazyColumn, spacing 24dp, padding ngang 16dp.
 */
@Composable
fun AnalyticsScreen(
    coupleId: String,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(coupleId) {
        viewModel.loadAnalytics(coupleId, 2026, 4) // Ví dụ cho tháng 4/2026
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Thống kê chi tiêu",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            AnalyticsShimmer(modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ChartCard(title = "Phân bổ chi tiêu") {
                        PieChart(data = uiState.categoryBreakdown)
                    }
                }

                item {
                    ChartCard(title = "Xu hướng thu chi") {
                        BarChart(data = uiState.spendingTrend)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun PieChart(data: List<CategoryBreakdown>) {
    val colors = listOf(
        Color(0xFF6200EE), Color(0xFF03DAC5), Color(0xFFBB86FC),
        Color(0xFF3700B3), Color(0xFF018786), Color(0xFFFF0266)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Canvas drawing: biểu đồ tròn sử dụng drawArc
        // useCenter=true nối các điểm biên vào tâm tạo hình quạt
        // startAngle tích lũy qua mỗi phần để vẽ đúng vị trí
        Canvas(modifier = Modifier.size(150.dp)) {
            var startAngle = 0f
            data.forEachIndexed { index, item ->
                val sweepAngle = item.percentage * 360f
                drawArc(
                    color = colors.getOrElse(index) { Color.Gray },
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                startAngle += sweepAngle
            }
        }

        Spacer(modifier = Modifier.width(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.take(5).forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(12.dp).background(colors.getOrElse(index) { Color.Gray }))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "${item.category}: ${(item.percentage * 100).toInt()}%", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BarChart(data: List<SpendingTrend>) {
    val maxVal = (data.maxOfOrNull { it.totalIncome.coerceAtLeast(it.totalExpense) } ?: 1000L).toFloat()
    
    // Canvas drawing: biểu đồ cột đôi (grouped bar chart)
    // barWidth = canvasWidth / (dataSize * 3) – mỗi group gồm 2 cột + 1 spacing
    // Vẽ từ dưới lên: topLeft.y = canvasHeight - barHeight
    Canvas(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        val barWidth = size.width / (data.size * 3)
        val spacing = barWidth
        
        data.forEachIndexed { index, trend ->
            val xIncome = index * (barWidth * 3)
            val xExpense = xIncome + barWidth
            
            val incomeHeight = (trend.totalIncome.toFloat() / maxVal) * size.height
            val expenseHeight = (trend.totalExpense.toFloat() / maxVal) * size.height
            
            // Draw Income Bar (Greenish)
            drawRect(
                color = Color(0xFF4CAF50),
                topLeft = Offset(xIncome, size.height - incomeHeight),
                size = Size(barWidth, incomeHeight)
            )
            
            // Draw Expense Bar (Reddish)
            drawRect(
                color = Color(0xFFF44336),
                topLeft = Offset(xExpense, size.height - expenseHeight),
                size = Size(barWidth, expenseHeight)
            )
        }
    }
}

@Composable
fun AnalyticsShimmer(modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        repeat(2) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatCurrency(amount: Long): String {
    return NumberFormat.getNumberInstance(Locale("vi", "VN")).format(amount) + " đ"
}