package com.example.mobileproject.data.mapper

import com.example.mobileproject.data.model.goal.GoalDto
import com.example.mobileproject.data.model.goal.GoalTaskDto
import com.example.mobileproject.domain.entity.*

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
            currentAmount = this.currentAmount ?: 0L,
            withdrawnAmount = this.withdrawnAmount ?: 0L,
        )
    }
}

fun GoalTaskDto.toDomain(): GoalTask {
    // Sử dụng Named Arguments để tránh nhầm lẫn vị trí tham số
    return GoalTask(
        taskId = this.taskId ?: "",
        content = this.content ?: "",
        isCompleted = this.isCompleted
    )
}
