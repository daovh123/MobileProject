package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.goal.GoalDto
import com.example.mobileproject.data.model.goal.GoalTaskDto
import com.example.mobileproject.domain.entity.*

/**
 * Ánh xạ [GoalDto] sang domain entity polymorphic.
 *
 * Logic phân biệt loại mục tiêu dựa trên trường [GoalDto.type]:
 * - `"FUTURE"` → [FutureGoal]: sử dụng [tasks] và [progress]
 * - Mặc định (bao gồm `"SAVING"`) → [SavingGoal]: sử dụng [targetAmount] và [currentAmount]
 *
 * Xử lý null safety:
 * - [id] fallback về [goalId] nếu null (tương thích 2 API response format)
 * - [coupleId], [name] fallback về chuỗi rỗng nếu null
 * - [category] mặc định "Others" nếu null
 * - [targetAmount]/[currentAmount] mặc định 0L nếu null
 * - [progress] mặc định 0.0 nếu null
 * - [tasks] mặc định emptyList() nếu null
 */
fun GoalDto.toDomain(): Goal {
    val statusEnum = GoalStatus.fromString(this.status)
    val typeStr = this.type ?: ""
    
    val idStr = this.id ?: this.goalId ?: ""
    val coupleIdStr = this.coupleId ?: ""
    val nameStr = this.name ?: ""
    val categoryStr = this.category ?: "Others"

    return if (typeStr == "FUTURE") {
        FutureGoal(
            id = idStr,
            coupleId = coupleIdStr,
            name = nameStr,
            category = categoryStr,
            deadline = this.deadline,
            status = statusEnum,
            createdAt = this.createdAt,
            tasks = this.tasks?.map { it.toDomain() } ?: emptyList(),
            progress = this.progress ?: 0.0
        )
    } else {
        SavingGoal(
            id = idStr,
            coupleId = coupleIdStr,
            name = nameStr,
            category = categoryStr,
            deadline = this.deadline,
            status = statusEnum,
            createdAt = this.createdAt,
            targetAmount = this.targetAmount ?: 0L,
            currentAmount = this.currentAmount ?: 0L
        )
    }
}

/**
 * Ánh xạ [GoalTaskDto] sang [GoalTask] domain entity.
 *
 * Xử lý null safety:
 * - [taskId] fallback về chuỗi rỗng nếu null
 * - [content] fallback về chuỗi rỗng nếu null (server có thể trả null)
 * - [isCompleted] giữ nguyên giá trị, mặc định false từ DTO
 */
fun GoalTaskDto.toDomain(): GoalTask {
    return GoalTask(
        taskId = this.taskId ?: "",
        content = this.content ?: "",
        isCompleted = this.isCompleted
    )
}
