package com.mobileproject.mobileprojectbackend.transaction;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.goal.GoalContribution;
import com.mobileproject.mobileprojectbackend.goal.GoalContributionRepository;
import com.mobileproject.mobileprojectbackend.goal.SavingGoal;
import com.mobileproject.mobileprojectbackend.goal.SavingGoalRepository;
import com.mobileproject.mobileprojectbackend.transaction.dto.TransactionResponse;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CoupleInfoRepository coupleInfoRepository;
    private final SavingGoalRepository savingGoalRepository;
    private final GoalContributionRepository goalContributionRepository;
    private final MongoTemplate mongoTemplate;

    public TransactionService(TransactionRepository transactionRepository,
                              CoupleInfoRepository coupleInfoRepository,
                              SavingGoalRepository savingGoalRepository,
                              GoalContributionRepository goalContributionRepository,
                              MongoTemplate mongoTemplate) {
        this.transactionRepository = transactionRepository;
        this.coupleInfoRepository = coupleInfoRepository;
        this.savingGoalRepository = savingGoalRepository;
        this.goalContributionRepository = goalContributionRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public TransactionResponse saveTransaction(String coupleId, Long amount, TransactionType type,
                                                String category, String note) {
        if (coupleId == null || coupleId.isBlank()) {
            return TransactionResponse.failure("Couple ID is required");
        }
        if (amount == null || amount <= 0) {
            return TransactionResponse.failure("Amount must be positive");
        }
        if (type == null) {
            return TransactionResponse.failure("Transaction type is required");
        }

        CoupleInfo coupleInfo = coupleInfoRepository.findById(coupleId).orElse(null);
        if (coupleInfo == null) {
            return TransactionResponse.failure("Couple not found");
        }

        Transaction transaction = new Transaction(coupleId, amount, type, category, note);
        Transaction savedTransaction = transactionRepository.save(transaction);

        long balanceChange = type == TransactionType.INCOME ? amount : -amount;

        Query query = new Query(Criteria.where("id").is(coupleId));
        Update update = new Update().inc("totalBalance", balanceChange);
        mongoTemplate.updateFirst(query, update, CoupleInfo.class);

        CoupleInfo updatedCouple = coupleInfoRepository.findById(coupleId).orElse(coupleInfo);

        return TransactionResponse.success(
                savedTransaction.getId(),
                amount,
                type.name(),
                category,
                note,
                updatedCouple.getTotalBalance()
        );
    }

    public TransactionResponse processIncome(String coupleId, Long amount, String targetType, 
                                              String goalId, String note) {
        if (coupleId == null || coupleId.isBlank()) {
            return TransactionResponse.failure("Couple ID is required");
        }
        if (amount == null || amount <= 0) {
            return TransactionResponse.failure("Amount must be positive");
        }
        if (targetType == null || targetType.isBlank()) {
            return TransactionResponse.failure("Target type is required");
        }

        CoupleInfo coupleInfo = coupleInfoRepository.findById(coupleId).orElse(null);
        if (coupleInfo == null) {
            return TransactionResponse.failure("Couple not found");
        }

        if ("WALLET".equalsIgnoreCase(targetType)) {
            Transaction transaction = new Transaction(coupleId, amount, TransactionType.INCOME, "INCOME", note);
            Transaction savedTransaction = transactionRepository.save(transaction);

            Query query = new Query(Criteria.where("id").is(coupleId));
            Update update = new Update().inc("totalBalance", amount);
            mongoTemplate.updateFirst(query, update, CoupleInfo.class);

            CoupleInfo updatedCouple = coupleInfoRepository.findById(coupleId).orElse(coupleInfo);

            return TransactionResponse.success(
                    savedTransaction.getId(),
                    amount,
                    "INCOME",
                    "INCOME",
                    note,
                    updatedCouple.getTotalBalance()
            );
        } else if ("GOAL".equalsIgnoreCase(targetType)) {
            if (goalId == null || goalId.isBlank()) {
                return TransactionResponse.failure("Goal ID is required for GOAL target type");
            }

            SavingGoal goal = savingGoalRepository.findById(goalId).orElse(null);
            if (goal == null) {
                return TransactionResponse.failure("Goal not found");
            }

            if (!goal.getCoupleId().equals(coupleId)) {
                return TransactionResponse.failure("Goal does not belong to this couple");
            }

            Query goalQuery = new Query(Criteria.where("id").is(goalId));
            Update goalUpdate = new Update().inc("currentAmount", amount);
            mongoTemplate.updateFirst(goalQuery, goalUpdate, SavingGoal.class);

            SavingGoal updatedGoal = savingGoalRepository.findById(goalId).orElse(goal);
            if (updatedGoal.getCurrentAmount() >= updatedGoal.getTargetAmount()) {
                Query statusUpdateQuery = new Query(Criteria.where("id").is(goalId));
                Update statusUpdate = new Update().set("status", com.mobileproject.mobileprojectbackend.goal.GoalStatus.ACHIEVED);
                mongoTemplate.updateFirst(statusUpdateQuery, statusUpdate, SavingGoal.class);
            }

            GoalContribution contribution = new GoalContribution(goalId, amount, coupleId, note);
            goalContributionRepository.save(contribution);

            return TransactionResponse.success(
                    null,
                    amount,
                    "INCOME",
                    "GOAL",
                    "Contributed to goal: " + goal.getName(),
                    null
            );
        } else {
            return TransactionResponse.failure("Invalid target type. Use WALLET or GOAL");
        }
    }
}