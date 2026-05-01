package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.GoalContributionResult
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ContributeToGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    /**
     * @param contributorId If null, it means contribute from main wallet.
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
