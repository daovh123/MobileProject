package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.mongodb.core.mapping.Field;

public class Task {

    @Field("task_id")
    private String taskId;

    private String content;

    @Field("is_completed")
    private boolean isCompleted;

    public Task() {
    }

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
