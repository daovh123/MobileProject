package com.mobileproject.mobileprojectbackend.goal;

import java.util.List;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.goal.dto.ContributeResponse;
import com.mobileproject.mobileprojectbackend.goal.dto.GoalResponse;

@Service
public class GoalService {

    private final SavingGoalRepository savingGoalRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final CoupleInfoRepository coupleInfoRepository;
    private final MongoTemplate mongoTemplate;

    public GoalService(SavingGoalRepository savingGoalRepository,
                       GoalContributionRepository goalContributionRepository,
                       CoupleInfoRepository coupleInfoRepository,
                       MongoTemplate mongoTemplate) {
        this.savingGoalRepository = savingGoalRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.coupleInfoRepository = coupleInfoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public GoalResponse createGoal(String coupleId, String name, String category, Long targetAmount, java.time.Instant deadline) {
        if (coupleId == null || coupleId.isBlank()) {
            return GoalResponse.failure("Couple ID is required");
        }
        if (name == null || name.isBlank()) {
            return GoalResponse.failure("Goal name is required");
        }
        if (targetAmount == null || targetAmount <= 0) {
            return GoalResponse.failure("Target amount must be positive");
        }

        CoupleInfo coupleInfo = coupleInfoRepository.findById(coupleId).orElse(null);
        if (coupleInfo == null) {
            return GoalResponse.failure("Couple not found");
        }

        SavingGoal goal = new SavingGoal(coupleId, name, category, targetAmount, deadline);
        SavingGoal savedGoal = savingGoalRepository.save(goal);

        return GoalResponse.success(
                savedGoal.getId(),
                savedGoal.getName(),
                savedGoal.getCategory(),
                savedGoal.getTargetAmount(),
                savedGoal.getCurrentAmount(),
                savedGoal.getStatus(),
                savedGoal.getDeadline(),
                savedGoal.getCreatedAt()
        );
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
            return ContributeResponse.failure("Goal not found");
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
        if (updatedGoal.getCurrentAmount() >= updatedGoal.getTargetAmount()) {
            Query statusUpdateQuery = new Query(Criteria.where("id").is(goalId));
            Update statusUpdate = new Update().set("status", GoalStatus.ACHIEVED);
            mongoTemplate.updateFirst(statusUpdateQuery, statusUpdate, SavingGoal.class);
            updatedGoal.setStatus(GoalStatus.ACHIEVED);
        }

        // For wallet contribution, we don't have a specific contributor, so we can set contributorId as null or a default like "WALLET"
        GoalContribution contribution = new GoalContribution(goalId, amount, null, note);
        GoalContribution savedContribution = goalContributionRepository.save(contribution);

        CoupleInfo updatedCouple = coupleInfoRepository.findById(goal.getCoupleId()).orElse(coupleInfo);

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
            return ContributeResponse.failure("Goal not found");
        }

        if (goal.getStatus() != GoalStatus.IN_PROGRESS) {
            return ContributeResponse.failure("Goal is not active");
        }

        Query goalQuery = new Query(Criteria.where("id").is(goalId));
        Update goalUpdate = new Update().inc("currentAmount", amount);
        mongoTemplate.updateFirst(goalQuery, goalUpdate, SavingGoal.class);

        SavingGoal updatedGoal = savingGoalRepository.findById(goalId).orElse(goal);
        if (updatedGoal.getCurrentAmount() >= updatedGoal.getTargetAmount()) {
            Query statusUpdateQuery = new Query(Criteria.where("id").is(goalId));
            Update statusUpdate = new Update().set("status", GoalStatus.ACHIEVED);
            mongoTemplate.updateFirst(statusUpdateQuery, statusUpdate, SavingGoal.class);
            updatedGoal.setStatus(GoalStatus.ACHIEVED);
        }

        GoalContribution contribution = new GoalContribution(goalId, amount, contributorId, note);
        GoalContribution savedContribution = goalContributionRepository.save(contribution);

        return ContributeResponse.success(
                savedContribution.getId(),
                goalId,
                amount,
                updatedGoal.getCurrentAmount(),
                null
        );
    }

    public List<SavingGoal> getGoalsByCouple(String coupleId) {
        if (coupleId == null || coupleId.isBlank()) {
            return List.of();
        }
        return savingGoalRepository.findByCoupleId(coupleId);
    }

    public SavingGoal getGoalById(String goalId) {
        return savingGoalRepository.findById(goalId).orElse(null);
    }
}