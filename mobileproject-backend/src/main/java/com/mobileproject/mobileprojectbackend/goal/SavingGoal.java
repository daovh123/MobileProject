package com.mobileproject.mobileprojectbackend.goal;

import java.time.Instant;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Entity mục tiêu tiết kiệm – lưu trong MongoDB collection {@code saving_goals}.
 *
 * <p>Kế thừa từ {@link Goal}. Đại diện cho mục tiêu có số tiền mục tiêu cụ thể,
 * theo dõi tiến độ bằng số tiền đã tiết kiệm được so với mục tiêu.</p>
 *
 * <h3>Ví dụ:</h3> Mua xe máy 30.000.000đ, đã tiết kiệm được 15.000.000đ.
 */
@Document(collection = "saving_goals")
public class SavingGoal extends Goal {

    /** Số tiền mục tiêu cần đạt (VND). */
    @Field("target_amount")
    private Long targetAmount;

    /** Số tiền đã tiết kiệm được (VND). Tăng dần qua các lần đóng góp. */
    @Field("current_amount")
    private Long currentAmount;

    /**
     * Constructor mặc định bắt buộc bởi MongoDB driver.
     */
    public SavingGoal() {
        super();
    }

    /**
     * Tạo mục tiêu tiết kiệm mới.
     *
     * @param coupleId    ID cặp đôi
     * @param name        tên mục tiêu
     * @param category    danh mục
     * @param targetAmount số tiền mục tiêu (VND)
     * @param deadline    thời hạn
     */
    public SavingGoal(String coupleId, String name, String category, Long targetAmount, Instant deadline) {
        super(coupleId, name, category, GoalType.SAVING, deadline);
        this.targetAmount = targetAmount;
        this.currentAmount = 0L;
    }

    public Long getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(Long targetAmount) {
        this.targetAmount = targetAmount;
    }

    public Long getCurrentAmount() {
        return currentAmount;
    }

    public void setCurrentAmount(Long currentAmount) {
        this.currentAmount = currentAmount;
    }
}