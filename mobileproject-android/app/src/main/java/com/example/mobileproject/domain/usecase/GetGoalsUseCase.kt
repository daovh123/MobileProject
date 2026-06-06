package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case lấy danh sách tất cả mục tiêu của cặp đôi.
 *
 * Cung cấp entry point đơn giản cho ViewModel để observe danh sách goals
 * mà không cần biết chi tiết implementation của repository.
 */
class GetGoalsUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    operator fun invoke(coupleId: String): Flow<Resource<List<Goal>>> {
        return repository.getGoals(coupleId)
    }
}
