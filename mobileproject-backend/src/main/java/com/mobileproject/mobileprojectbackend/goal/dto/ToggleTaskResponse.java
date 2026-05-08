package com.mobileproject.mobileprojectbackend.goal.dto;

public record ToggleTaskResponse(
        boolean success,
        String message,
        String goalId,
        String taskId,
        Double progress
) {
    public static ToggleTaskResponse success(String goalId, String taskId, Double progress) {
        return new ToggleTaskResponse(true, "Task toggled successfully", goalId, taskId, progress);
    }

    public static ToggleTaskResponse failure(String message) {
        return new ToggleTaskResponse(false, message, null, null, null);
    }
}
