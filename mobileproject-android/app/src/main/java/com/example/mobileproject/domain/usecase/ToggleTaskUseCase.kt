package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ToggleTaskUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    operator fun invoke(goalId: String, taskId: String): Flow<Resource<Double>> {
        return repository.toggleTask(goalId, taskId)
    }
}
