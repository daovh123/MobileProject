package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case đánh dấu hoàn thành/chưa hoàn thành một nhiệm vụ trong mục tiêu.
 *
 * Toggle trạng thái task và trả về tiến độ mới (0.0 - 100.0) của mục tiêu,
 * giúp UI cập nhật thanh tiến độ ngay lập tức.
 */
class ToggleTaskUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    operator fun invoke(goalId: String, taskId: String): Flow<Resource<Double>> {
        return repository.toggleTask(goalId, taskId)
    }
}
