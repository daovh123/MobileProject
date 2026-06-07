package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WithdrawGoalToWalletUseCase @Inject constructor(
    private val repository: GoalRepository,
) {
    operator fun invoke(goalId: String): Flow<Resource<GoalContributionResult>> {
        return repository.withdrawToWallet(goalId)
    }
}
