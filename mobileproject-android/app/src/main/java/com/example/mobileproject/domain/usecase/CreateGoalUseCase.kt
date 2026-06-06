package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.entity.GoalTask
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case tạo mục tiêu mới cho cặp đôi.
 *
 * Encapsulate logic tạo mục tiêu với hai loại:
 * - **saving**: mục tiêu tiết kiệm có số tiền mục tiêu và hạn chót
 * - **task**: mục tiêu gồm danh sách nhiệm vụ cần hoàn thành
 *
 * Use case này tồn tại để cô lập logic tạo goal khỏi ViewModel,
 * đảm bảo nguyên tắc Single Responsibility và dễ dàng test.
 */
class CreateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    operator fun invoke(
        coupleId: String,
        name: String,
        category: String,
        type: String,
        targetAmount: Long? = null,
        deadline: String? = null,
        tasks: List<GoalTask>? = null
    ): Flow<Resource<Goal>> {
        return repository.createGoal(coupleId, name, category, type, targetAmount, deadline, tasks)
    }
}
