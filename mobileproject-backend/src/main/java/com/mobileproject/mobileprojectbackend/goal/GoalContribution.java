package com.mobileproject.mobileprojectbackend.goal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

/**
 * Entity ghi nhận mỗi lần đóng góp tiền vào mục tiêu tiết kiệm.
 *
 * <p>Lưu trong MongoDB collection {@code goal_contributions}.
 * Mỗi bản ghi đại diện cho một lần nộp tiền vào {@link SavingGoal}.</p>
 *
 * <h3>Indexes:</h3>
 * <ul>
 *   <li>{@code id_goal} – indexed, truy vấn nhanh các đóng góp theo mục tiêu</li>
 * </ul>
 *
 * <h3>Mối quan hệ:</h3>
 * <ul>
 *   <li>{@code goalId} → tham chiếu đến {@code SavingGoal.id}</li>
 *   <li>{@code contributorId} → tham chiếu đến ID người đóng góp (có thể null)</li>
 * </ul>
 */
@Document(collection = "goal_contributions")
public class GoalContribution {

    /** ID duy nhất của bản ghi đóng góp (MongoDB ObjectId). */
    @Id
    private String id;

    /** ID mục tiêu tiết kiệm được đóng góp. */
    @Indexed
    @Field("id_goal")
    private String goalId;

    /** Số tiền đóng góp (VND). */
    private Long amount;

    /** ID người đóng góp (có thể null nếu đóng góp từ ví chung). */
    @Field("contributor_id")
    private String contributorId;

    /** Ghi chú cho lần đóng góp. */
    private String note;

    /** Thời điểm đóng góp. */
    private Instant timestamp;

    /**
     * Constructor mặc định bắt buộc bởi MongoDB driver.
     */
    public GoalContribution() {
    }

    /**
     * Tạo bản ghi đóng góp mới với thời gian là thời điểm hiện tại.
     *
     * @param goalId        ID mục tiêu
     * @param amount        số tiền đóng góp (VND)
     * @param contributorId ID người đóng góp (có thể null)
     * @param note          ghi chú
     */
    public GoalContribution(String goalId, Long amount, String contributorId, String note) {
        this.goalId = goalId;
        this.amount = amount;
        this.contributorId = contributorId;
        this.note = note;
        this.timestamp = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGoalId() {
        return goalId;
    }

    public void setGoalId(String goalId) {
        this.goalId = goalId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getContributorId() {
        return contributorId;
    }

    public void setContributorId(String contributorId) {
        this.contributorId = contributorId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}