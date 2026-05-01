package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSavingGoalsUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    operator fun invoke(coupleId: String): Flow<Resource<List<SavingGoal>>> {
        return repository.getGoals(coupleId)
    }
}
