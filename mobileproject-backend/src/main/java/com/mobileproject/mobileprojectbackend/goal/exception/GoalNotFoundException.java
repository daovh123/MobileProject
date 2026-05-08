package com.mobileproject.mobileprojectbackend.goal.exception;

public class GoalNotFoundException extends RuntimeException {
    public GoalNotFoundException(String goalId) {
        super("Goal not found with id: " + goalId);
    }

    public GoalNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
