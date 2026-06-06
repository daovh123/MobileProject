package com.mobileproject.mobileprojectbackend.goal;

import com.mobileproject.mobileprojectbackend.goal.dto.ContributeResponse;
import com.mobileproject.mobileprojectbackend.goal.dto.GoalResponse;
import com.mobileproject.mobileprojectbackend.goal.dto.TaskDto;
import com.mobileproject.mobileprojectbackend.goal.dto.ToggleTaskResponse;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.notifications.NotificationService;
import com.mobileproject.mobileprojectbackend.notifications.NotificationType;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service xử lý logic nghiệp vụ cho mục tiêu (Goals) của cặp đôi.
 *
 * <p><b>Nghiệp vụ chính:</b></p>
 * <ul>
 *   <li>Tạo mục tiêu mới (SAVING hoặc FUTURE)</li>
 *   <li>Đóng góp tiền vào mục tiêu tiết kiệm từ ví chung hoặc trực tiếp</li>
 *   <li>Quản lý tasks trong mục tiêu tương lai (toggle completion)</li>
 *   <li>Tra cứu mục tiêu theo cặp đôi hoặc ID</li>
 * </ul>
 *
 * <p><b>Bảo toàn dữ liệu:</b></p>
 * <ul>
 *   <li>Sử dụng {@link MongoTemplate#updateFirst} cho atomic increment trên cả
 *       {@code CoupleInfo.totalBalance} và {@code SavingGoal.currentAmount}</li>
 *   <li>Kiểm tra và cập nhật trạng thái {@code ACHIEVED} khi {@code currentAmount >= targetAmount}</li>
 * </ul>
 */
@Service
public class GoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final FutureGoalRepository futureGoalRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final CoupleInfoRepository coupleInfoRepository;
    private final MongoTemplate mongoTemplate;
    private final NotificationService notificationService;

    public GoalService(SavingGoalRepository savingGoalRepository,
                       FutureGoalRepository futureGoalRepository,
                       GoalContributionRepository goalContributionRepository,
                       CoupleInfoRepository coupleInfoRepository,
                       MongoTemplate mongoTemplate,
                       NotificationService notificationService) {
        this.savingGoalRepository = savingGoalRepository;
        this.futureGoalRepository = futureGoalRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.coupleInfoRepository = coupleInfoRepository;
        this.mongoTemplate = mongoTemplate;
        this.notificationService = notificationService;
    }

    /**
     * Tạo mục tiêu mới cho cặp đôi.
     *
     * <p><b>Khi type = SAVING:</b></p>
     * <ul>
     *   <li>Yêu cầu targetAmount &gt; 0</li>
     *   <li>Tạo {@link SavingGoal} trong collection {@code saving_goals}</li>
     * </ul>
     *
     * <p><b>Khi type = FUTURE:</b></p>
     * <ul>
     *   <li>Tạo {@link FutureGoal} với danh sách tasks ban đầu</li>
     *   <li>Mỗi task được gán UUID nếu chưa có taskId</li>
     * </ul>
     *
     * <p>Gửi thông báo "Mục tiêu mới được tạo" cho cả hai thành viên.</p>
     *
     * @param coupleId     ID cặp đôi
     * @param name         tên mục tiêu
     * @param category     danh mục
     * @param type         loại mục tiêu (SAVING / FUTURE)
     * @param targetAmount số tiền mục tiêu (bắt buộc cho SAVING)
     * @param deadline     thời hạn
     * @param tasks        danh sách tasks (chỉ dùng cho FUTURE)
     * @return {@link GoalResponse} chứa thông tin mục tiêu đã tạo
     */
    public GoalResponse createGoal(String coupleId, String name, String category,
                                    GoalType type, Long targetAmount,
                                    Instant deadline, List<TaskDto> tasks) {
        if (coupleId == null || coupleId.isBlank()) {
            return GoalResponse.failure("Couple ID is required");
        }
        if (name == null || name.isBlank()) {
            return GoalResponse.failure("Goal name is required");
        }

        CoupleInfo coupleInfo = coupleInfoRepository.findById(coupleId).orElse(null);
        if (coupleInfo == null) {
            return GoalResponse.failure("Couple not found");
        }

        if (type == GoalType.SAVING) {
            if (targetAmount == null || targetAmount <= 0) {
                return GoalResponse.failure("Target amount must be positive for saving goals");
            }
            SavingGoal goal = new SavingGoal(coupleId, name, category, targetAmount, deadline);
            SavingGoal savedGoal = savingGoalRepository.save(goal);

            notificationService.createAndPushForCouple(coupleId, NotificationType.GOAL_CREATED,
                    "Mục tiêu mới được tạo",
                    "Mục tiêu tiết kiệm \"" + name + "\" vừa được thêm!");

            return GoalResponse.success(
                    savedGoal.getId(),
                    savedGoal.getName(),
                    savedGoal.getCategory(),
                    savedGoal.getGoalTypeEnum(),
                    savedGoal.getStatus(),
                    savedGoal.getDeadline(),
                    savedGoal.getCreatedAt(),
                    savedGoal.getTargetAmount(),
                    savedGoal.getCurrentAmount(),
                    null,
                    null
            );
        } else if (type == GoalType.FUTURE) {
            List<Task> taskEntities = tasks != null ? tasks.stream()
                    .map(taskDto -> new Task(
                            taskDto.taskId() != null ? taskDto.taskId() : UUID.randomUUID().toString(),
                            taskDto.content(),
                            false
                    ))
                    .collect(Collectors.toList()) : new ArrayList<>();

            FutureGoal goal = new FutureGoal(coupleId, name, category, taskEntities, deadline);
            FutureGoal savedGoal = futureGoalRepository.save(goal);

            notificationService.createAndPushForCouple(coupleId, NotificationType.GOAL_CREATED,
                    "Mục tiêu mới được tạo",
                    "Mục tiêu tương lai \"" + name + "\" vừa được thêm!");

            List<TaskDto> savedTaskDtos = savedGoal.getTasks().stream()
                    .map(task -> new TaskDto(task.getTaskId(), task.getContent(), task.isCompleted()))
                    .collect(Collectors.toList());

            return GoalResponse.success(
                    savedGoal.getId(),
                    savedGoal.getName(),
                    savedGoal.getCategory(),
                    savedGoal.getGoalTypeEnum(),
                    savedGoal.getStatus(),
                    savedGoal.getDeadline(),
                    savedGoal.getCreatedAt(),
                    null,
                    null,
                    savedGoal.getProgress(),
                    savedTaskDtos
            );
        }

        return GoalResponse.failure("Invalid goal type");
    }

    /**
     * Đóng góp tiền vào mục tiêu tiết kiệm từ ví chung.
     *
     * <p><b>Luồng xử lý:</b></p>
     * <ol>
     *   <li>Validate goalId, amount</li>
     *   <li>Tìm mục tiêu và kiểm tra trạng thái IN_PROGRESS</li>
     *   <li>Kiểm tra số dư ví chung đủ</li>
     *   <li>Atomic: trừ tiền ví chung và cộng vào mục tiêu</li>
     *   <li>Kiểm tra nếu mục tiêu đạt được → cập nhật ACHIEVED</li>
     *   <li>Lưu bản ghi {@link GoalContribution}</li>
     *   <li>Gửi thông báo cho cả hai thành viên</li>
     * </ol>
     *
     * @param goalId ID mục tiêu tiết kiệm
     * @param amount số tiền đóng góp (VND)
     * @param note   ghi chú
     * @return {@link ContributeResponse} với thông tin đóng góp và số dư cập nhật
     */
    public ContributeResponse contributeFromWallet(String goalId, Long amount, String note) {
        if (goalId == null || goalId.isBlank()) {
            return ContributeResponse.failure("Goal ID is required");
        }
        if (amount == null || amount <= 0) {
            return ContributeResponse.failure("Amount must be positive");
        }

        SavingGoal goal = savingGoalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ContributeResponse.failure("Goal not found or not a saving goal");
        }

        if (goal.getStatus() != GoalStatus.IN_PROGRESS) {
            return ContributeResponse.failure("Goal is not active");
        }

        CoupleInfo coupleInfo = coupleInfoRepository.findById(goal.getCoupleId()).orElse(null);
        if (coupleInfo == null) {
            return ContributeResponse.failure("Couple not found");
        }

        Long currentBalance = coupleInfo.getTotalBalance() != null ? coupleInfo.getTotalBalance() : 0L;
        if (currentBalance < amount) {
            return ContributeResponse.failure("Insufficient wallet balance");
        }

        Query coupleQuery = new Query(Criteria.where("id").is(goal.getCoupleId()));
        Update coupleUpdate = new Update().inc("totalBalance", -amount);
        mongoTemplate.updateFirst(coupleQuery, coupleUpdate, CoupleInfo.class);

        Query goalQuery = new Query(Criteria.where("id").is(goalId));
        Update goalUpdate = new Update().inc("currentAmount", amount);
        mongoTemplate.updateFirst(goalQuery, goalUpdate, SavingGoal.class);

        SavingGoal updatedGoal = savingGoalRepository.findById(goalId).orElse(goal);
        boolean achieved = updatedGoal.getCurrentAmount() >= updatedGoal.getTargetAmount();
        if (achieved) {
            Query statusUpdateQuery = new Query(Criteria.where("id").is(goalId));
            Update statusUpdate = new Update().set("status", GoalStatus.ACHIEVED);
            mongoTemplate.updateFirst(statusUpdateQuery, statusUpdate, SavingGoal.class);
            updatedGoal.setStatus(GoalStatus.ACHIEVED);
        }

        GoalContribution contribution = new GoalContribution(goalId, amount, null, note);
        GoalContribution savedContribution = goalContributionRepository.save(contribution);

        CoupleInfo updatedCouple = coupleInfoRepository.findById(goal.getCoupleId()).orElse(coupleInfo);

        String formattedAmount = String.format("%,d đ", amount);
        notificationService.createAndPushForCouple(goal.getCoupleId(), NotificationType.GOAL_UPDATED,
                "Đã đóng góp vào mục tiêu",
                "Thêm " + formattedAmount + " vào \"" + goal.getName() + "\"");

        if (achieved) {
            notificationService.createAndPushForCouple(goal.getCoupleId(), NotificationType.GOAL_COMPLETED,
                    "Mục tiêu hoàn thành! 🎉",
                    "Mục tiêu \"" + goal.getName() + "\" đã đạt được!");
        }

        return ContributeResponse.success(
                savedContribution.getId(),
                goalId,
                amount,
                updatedGoal.getCurrentAmount(),
                updatedCouple.getTotalBalance()
        );
    }

    /**
     * Đóng góp tiền trực tiếp vào mục tiêu (không trừ từ ví chung).
     *
     * <p>Dùng khi nạp tiền trực tiếp vào mục tiêu qua hệ thống thanh toán
     * (ví dụ: SePay top-up với targetType = "GOAL").</p>
     *
     * @param goalId        ID mục tiêu tiết kiệm
     * @param amount        số tiền đóng góp (VND)
     * @param contributorId ID người đóng góp
     * @param note          ghi chú
     * @return {@link ContributeResponse} với thông tin đóng góp
     */
    public ContributeResponse contributeToGoalDirect(String goalId, Long amount, String contributorId, String note) {
        if (goalId == null || goalId.isBlank()) {
            return ContributeResponse.failure("Goal ID is required");
        }
        if (amount == null || amount <= 0) {
            return ContributeResponse.failure("Amount must be positive");
        }

        SavingGoal goal = savingGoalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ContributeResponse.failure("Goal not found or not a saving goal");
        }

        if (goal.getStatus() != GoalStatus.IN_PROGRESS) {
            return ContributeResponse.failure("Goal is not active");
        }

        Query goalQuery = new Query(Criteria.where("id").is(goalId));
        Update goalUpdate = new Update().inc("currentAmount", amount);
        mongoTemplate.updateFirst(goalQuery, goalUpdate, SavingGoal.class);

        SavingGoal updatedGoal = savingGoalRepository.findById(goalId).orElse(goal);
        boolean achieved = updatedGoal.getCurrentAmount() >= updatedGoal.getTargetAmount();
        if (achieved) {
            Query statusUpdateQuery = new Query(Criteria.where("id").is(goalId));
            Update statusUpdate = new Update().set("status", GoalStatus.ACHIEVED);
            mongoTemplate.updateFirst(statusUpdateQuery, statusUpdate, SavingGoal.class);
            updatedGoal.setStatus(GoalStatus.ACHIEVED);
        }

        GoalContribution contribution = new GoalContribution(goalId, amount, contributorId, note);
        goalContributionRepository.save(contribution);

        String formattedAmount = String.format("%,d đ", amount);
        notificationService.createAndPushForCouple(goal.getCoupleId(), NotificationType.GOAL_UPDATED,
                "Đã đóng góp vào mục tiêu",
                "Thêm " + formattedAmount + " vào \"" + goal.getName() + "\"");

        if (achieved) {
            notificationService.createAndPushForCouple(goal.getCoupleId(), NotificationType.GOAL_COMPLETED,
                    "Mục tiêu hoàn thành! 🎉",
                    "Mục tiêu \"" + goal.getName() + "\" đã đạt được!");
        }

        return ContributeResponse.success(
                contribution.getId(),
                goalId,
                amount,
                updatedGoal.getCurrentAmount(),
                null
        );
    }

    /**
     * Lấy tất cả mục tiêu (cả SAVING và FUTURE) của một cặp đôi.
     *
     * @param coupleId ID cặp đôi
     * @return danh sách mục tiêu gộp từ cả hai loại
     */
    public List<Goal> getGoalsByCouple(String coupleId) {
        if (coupleId == null || coupleId.isBlank()) {
            return List.of();
        }
        List<SavingGoal> savingGoals = savingGoalRepository.findByCoupleId(coupleId);
        List<FutureGoal> futureGoals = futureGoalRepository.findByCoupleId(coupleId);

        return Stream.concat(
                savingGoals.stream().map(goal -> (Goal) goal),
                futureGoals.stream().map(goal -> (Goal) goal)
        ).collect(Collectors.toList());
    }

    /**
     * Tìm mục tiêu theo ID (thử cả SavingGoal và FutureGoal).
     *
     * @param goalId ID mục tiêu
     * @return {@link Goal} nếu tìm thấy, null nếu không
     */
    public Goal getGoalById(String goalId) {
        return savingGoalRepository.findById(goalId)
                .map(goal -> (Goal) goal)
                .orElse(futureGoalRepository.findById(goalId).orElse(null));
    }

    public SavingGoal getSavingGoalById(String goalId) {
        return savingGoalRepository.findById(goalId).orElse(null);
    }

    public FutureGoal getFutureGoalById(String goalId) {
        return futureGoalRepository.findById(goalId).orElse(null);
    }

    /**
     * Toggle trạng thái hoàn thành của task trong mục tiêu tương lai.
     *
     * <p><b>Luồng xử lý:</b></p>
     * <ol>
     *   <li>Tìm FutureGoal theo goalId</li>
     *   <li>Tìm task theo taskId trong danh sách tasks</li>
     *   <li>Đảo trạng thái completed → tự động tính lại progress</li>
     *   <li>Nếu progress ≥ 100% → gửi thông báo "Mục tiêu hoàn thành"</li>
     * </ol>
     *
     * @param goalId ID mục tiêu tương lai
     * @param taskId ID công việc cần toggle
     * @return {@link ToggleTaskResponse} với tiến độ cập nhật
     */
    public ToggleTaskResponse toggleTask(String goalId, String taskId) {
        if (goalId == null || goalId.isBlank()) {
            return ToggleTaskResponse.failure("Goal ID is required");
        }
        if (taskId == null || taskId.isBlank()) {
            return ToggleTaskResponse.failure("Task ID is required");
        }

        FutureGoal goal = futureGoalRepository.findById(goalId).orElse(null);
        if (goal == null) {
            return ToggleTaskResponse.failure("Future goal not found");
        }

        Task task = goal.findTaskById(taskId);
        if (task == null) {
            return ToggleTaskResponse.failure("Task not found in this goal");
        }

        goal.toggleTaskCompletion(taskId);
        FutureGoal savedGoal = futureGoalRepository.save(goal);

        // Notify if goal completed
        if (savedGoal.getProgress() >= 100.0) {
            notificationService.createAndPushForCouple(savedGoal.getCoupleId(), NotificationType.GOAL_COMPLETED,
                    "Mục tiêu hoàn thành! 🎉",
                    "Mục tiêu \"" + savedGoal.getName() + "\" đã hoàn thành!");
        }

        return ToggleTaskResponse.success(goalId, taskId, savedGoal.getProgress());
    }
}
