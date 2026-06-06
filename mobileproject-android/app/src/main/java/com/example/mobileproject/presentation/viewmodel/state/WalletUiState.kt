package com.example.mobileproject.presentation.viewmodel.state

import com.example.mobileproject.domain.entity.CategoryBreakdown
import com.example.mobileproject.domain.entity.SpendingTrend
import com.example.mobileproject.domain.entity.Transaction
import com.example.mobileproject.domain.entity.Wallet

/**
 * Trạng thái UI cho màn hình Ví (Wallet).
 *
 * @property isLoading True khi đang tải dữ liệu ví, phân tích, và giao dịch
 * @property wallet Thông tin ví chung của cặp đôi (số dư, ...), null nếu chưa tải
 * @property categoryBreakdown Phân tích chi tiêu theo danh mục, sắp xếp từ lớn đến bé.
 *   Mỗi item chứa tên danh mục, tổng tiền, số lượng giao dịch, v.v.
 *   UI dùng để render biểu đồ tròn/bar chart
 * @property spendingTrend Xu hướng chi tiêu theo tháng trong năm.
 *   Mỗi item chứa tháng và tổng chi tiêu. UI dùng để render line chart
 * @property recentTransactions 5 giao dịch gần nhất (subset của [allTransactions]).
 *   Hiển thị trên dashboard/wallet card
 * @property allTransactions Tất cả giao dịch của cặp đôi, sắp xếp theo thời gian giảm dần.
 *   Hiển thị khi người dùng xem toàn bộ lịch sử giao dịch
 * @property error Thông báo lỗi (null nếu không có lỗi)
 */
data class WalletUiState(
    val isLoading: Boolean = false,
    val wallet: Wallet? = null,
    val categoryBreakdown: List<CategoryBreakdown> = emptyList(),
    val spendingTrend: List<SpendingTrend> = emptyList(),
    val recentTransactions: List<Transaction> = emptyList(),
    val allTransactions: List<Transaction> = emptyList(),
    val error: String? = null
)
