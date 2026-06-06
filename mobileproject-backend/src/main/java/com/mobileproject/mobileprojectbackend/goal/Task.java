package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity công việc (task)嵌入 trong {@link FutureGoal}.
 *
 * <p>Không có collection riêng – được lưu dưới dạng embedded document
 * trong trường {@code tasks} của {@code FutureGoal}.</p>
 */
public class Task {

    /** ID duy nhất của task (UUID). */
    @Field("task_id")
    private String taskId;

    /** Nội dung mô tả công việc. */
    private String content;

    /** Trạng thái hoàn thành: {@code true} = đã xong. */
    @Field("is_completed")
    private boolean isCompleted;

    /**
     * Constructor mặc định.
     */
    public Task() {
    }

    /**
     * Tạo task mới.
     *
     * @param taskId      ID duy nhất
     * @param content     nội dung công việc
     * @param isCompleted trạng thái hoàn thành
     */
    public Task(String taskId, String content, boolean isCompleted) {
        this.taskId = taskId;
        this.content = content;
        this.isCompleted = isCompleted;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
