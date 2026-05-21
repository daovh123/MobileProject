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
