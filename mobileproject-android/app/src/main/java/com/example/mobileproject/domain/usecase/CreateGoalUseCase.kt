package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.Goal
import com.example.mobileproject.domain.entity.GoalTask
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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
