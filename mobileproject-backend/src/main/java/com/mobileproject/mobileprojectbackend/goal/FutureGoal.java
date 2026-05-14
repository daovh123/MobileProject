package com.mobileproject.mobileprojectbackend.goal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "future_goals")
public class FutureGoal extends Goal {

    @Field("tasks")
    private List<Task> tasks = new ArrayList<>();

    @Field("progress")
    private Double progress;

    public FutureGoal() {
        super();
    }

    public FutureGoal(String coupleId, String name, String category, List<Task> tasks, Instant deadline) {
        super(coupleId, name, category, GoalType.FUTURE, deadline);
        this.tasks = tasks != null ? tasks : new ArrayList<>();
        this.recalculateProgress();
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        this.recalculateProgress();
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
        this.recalculateProgress();
    }

    public void removeTask(String taskId) {
        this.tasks.removeIf(task -> task.getTaskId().equals(taskId));
        this.recalculateProgress();
    }

    public Task findTaskById(String taskId) {
        return tasks.stream()
                .filter(task -> task.getTaskId().equals(taskId))
                .findFirst()
                .orElse(null);
    }

    public void toggleTaskCompletion(String taskId) {
        Task task = findTaskById(taskId);
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            this.recalculateProgress();
        }
    }

    private void recalculateProgress() {
        if (tasks == null || tasks.isEmpty()) {
            this.progress = 0.0;
        } else {
            long completedCount = tasks.stream().filter(Task::isCompleted).count();
            this.progress = (double) completedCount / tasks.size() * 100.0;
        }
    }
}
