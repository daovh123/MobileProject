package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.SavingGoal
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
        targetAmount: Long,
        deadline: String?
    ): Flow<Resource<SavingGoal>> {
        return repository.createGoal(coupleId, name, category, targetAmount, deadline)
    }
}
