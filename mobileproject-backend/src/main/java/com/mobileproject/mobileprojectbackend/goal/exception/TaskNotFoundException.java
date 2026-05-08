package com.mobileproject.mobileprojectbackend.goal.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String taskId) {
        super("Task not found with id: " + taskId);
    }

    public TaskNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
