package com.mobileproject.mobileprojectbackend.goal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity mục tiêu tương lai – lưu trong MongoDB collection {@code future_goals}.
 *
 * <p>Kế thừa từ {@link Goal}. Đại diện cho mục tiêu dạng checklist công việc,
 * theo dõi tiến độ bằng tỷ lệ phần trăm tasks đã hoàn thành.</p>
 *
 * <h3>Ví dụ:</h3> Kế hoạch du lịch Đà Nẵng với các công việc: đặt vé máy bay, đặt khách sạn, ...
 */
@Document(collection = "future_goals")
public class FutureGoal extends Goal {

    /** Danh sách công việc (tasks) thuộc mục tiêu. */
    @Field("tasks")
    private List<Task> tasks = new ArrayList<>();

    /** Tiến độ hoàn thành (0.0 – 100.0%), tính tự động từ tỷ lệ tasks hoàn thành. */
    @Field("progress")
    private Double progress;

    /**
     * Constructor mặc định bắt buộc bởi MongoDB driver.
     */
    public FutureGoal() {
        super();
    }

    /**
     * Tạo mục tiêu tương lai mới với danh sách tasks.
     *
     * @param coupleId ID cặp đôi
     * @param name     tên mục tiêu
     * @param category danh mục
     * @param tasks    danh sách công việc ban đầu (có thể null)
     * @param deadline thời hạn
     */
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

    /**
     * Thêm task mới vào mục tiêu và tự động tính lại tiến độ.
     *
     * @param task công việc cần thêm
     */
    public void addTask(Task task) {
        this.tasks.add(task);
        this.recalculateProgress();
    }

    /**
     * Xóa task theo ID và tự động tính lại tiến độ.
     *
     * @param taskId ID công việc cần xóa
     */
    public void removeTask(String taskId) {
        this.tasks.removeIf(task -> task.getTaskId().equals(taskId));
        this.recalculateProgress();
    }

    /**
     * Tìm task theo ID.
     *
     * @param taskId ID công việc
     * @return {@link Task} nếu tìm thấy, null nếu không
     */
    public Task findTaskById(String taskId) {
        return tasks.stream()
                .filter(task -> task.getTaskId().equals(taskId))
                .findFirst()
                .orElse(null);
    }

    /**
     * Đảo ngược trạng thái hoàn thành của task (completed ↔ not completed)
     * và tự động tính lại tiến độ.
     *
     * @param taskId ID công việc cần toggle
     */
    public void toggleTaskCompletion(String taskId) {
        Task task = findTaskById(taskId);
        if (task != null) {
            task.setCompleted(!task.isCompleted());
            this.recalculateProgress();
        }
    }

    /**
     * Tính lại tiến độ dựa trên tỷ lệ tasks đã hoàn thành.
     * Công thức: {@code (completedTasks / totalTasks) * 100}.
     * Nếu không có task nào → progress = 0.0.
     */
    private void recalculateProgress() {
        if (tasks == null || tasks.isEmpty()) {
            this.progress = 0.0;
        } else {
            long completedCount = tasks.stream().filter(Task::isCompleted).count();
            this.progress = (double) completedCount / tasks.size() * 100.0;
        }
    }
}
