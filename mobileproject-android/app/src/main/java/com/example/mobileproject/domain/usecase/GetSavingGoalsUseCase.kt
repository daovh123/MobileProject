package com.example.mobileproject.domain.usecase

import com.example.mobileproject.core.result.Resource
import com.example.mobileproject.domain.entity.SavingGoal
import com.example.mobileproject.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case lấy danh sách mục tiêu tiết kiệm (saving goals) của cặp đôi.
 *
 * Khác với [GetGoalsUseCase] trả về TẤT CẢ goals, use case này
 * chỉ trả về các mục tiêu thuộc loại "saving" (tiết kiệm) bằng cách
 * lọc và map từ List<Goal> sang List<SavingGoal>.
 *
 * Việc lọc ở domain layer (không phải data layer) giúp repository
 * vẫn trả về dữ liệu đầy đủ, trong khi use case đảm bảo
 * UI chỉ nhận đúng loại dữ liệu cần hiển thị.
 */
class GetSavingGoalsUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    operator fun invoke(coupleId: String): Flow<Resource<List<SavingGoal>>> {
        return repository.getGoals(coupleId).map { resource ->
            when (resource) {
                is Resource.Success -> {
                    // Chuyển đổi List<Goal> thành List<SavingGoal>
                    val filteredGoals = resource.data.filterIsInstance<SavingGoal>()
                    Resource.Success(filteredGoals)
                }
                is Resource.Error -> {
                    // Resource.Error kế thừa Resource<Nothing>, tự động tương thích với Resource<List<SavingGoal>>
                    resource
                }
                else -> {
                    // Nhánh này xử lý Resource.Loading mà không cần gọi tên trực tiếp
                    // Ép kiểu an toàn vì Resource.Loading cũng là Resource<Nothing>
                    @Suppress("UNCHECKED_CAST")
                    resource as Resource<List<SavingGoal>>
                }
            }
        }
    }
}
