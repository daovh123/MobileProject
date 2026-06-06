package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case xử lý logic đóng góp tiền vào mục tiêu tiết kiệm.
 *
 * Encapsulate việc quyết định cách thức đóng góp:
 * - Nếu [contributorId] là null → đóng góp từ ví chung (tự động trừ số dư ví)
 * - Nếu [contributorId] khác null → đóng góp trực tiếp từ một thành viên
 *
 * Việc tách biệt hai cách đóng góp thành một use case giúp ViewModel
 * không cần quan tâm chi tiết triển khai, chỉ cần gọi invoke().
 */
class ContributeToGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    /**
     * @param contributorId ID người đóng góp. Null = đóng góp từ ví chung, khác null = đóng góp trực tiếp.
     */
    operator fun invoke(
        goalId: String,
        amount: Long,
        note: String?,
        contributorId: String? = null
    ): Flow<Resource<GoalContributionResult>> {
        return if (contributorId == null) {
            repository.contributeFromWallet(goalId, amount, note)
        } else {
            repository.contributeDirect(goalId, amount, contributorId, note)
        }
    }
}
