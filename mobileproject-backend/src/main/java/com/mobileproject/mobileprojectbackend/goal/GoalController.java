package com.mobileproject.mobileprojectbackend.goal;

import com.mobileproject.mobileprojectbackend.goal.dto.ContributeRequest;
import com.mobileproject.mobileprojectbackend.goal.dto.ContributeResponse;
import com.mobileproject.mobileprojectbackend.goal.dto.CreateGoalRequest;
import com.mobileproject.mobileprojectbackend.goal.dto.GoalResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping
    public ResponseEntity<GoalResponse> createGoal(@RequestBody CreateGoalRequest request) {
        GoalResponse response = goalService.createGoal(
                request.coupleId(),
                request.name(),
                request.targetAmount(),
                request.deadline()
        );
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/{goalId}/contribute-from-wallet")
    public ResponseEntity<ContributeResponse> contributeFromWallet(
            @PathVariable String goalId,
            @RequestBody ContributeRequest request) {
        ContributeResponse response = goalService.contributeFromWallet(
                goalId,
                request.amount(),
                request.contributorId(),
                request.note()
        );
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/{goalId}/contribute")
    public ResponseEntity<ContributeResponse> contributeToGoal(
            @PathVariable String goalId,
            @RequestBody ContributeRequest request) {
        ContributeResponse response = goalService.contributeToGoalDirect(
                goalId,
                request.amount(),
                request.contributorId(),
                request.note()
        );
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/couple/{coupleId}")
    public ResponseEntity<List<SavingGoal>> getGoalsByCouple(@PathVariable String coupleId) {
        return ResponseEntity.ok(goalService.getGoalsByCouple(coupleId));
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<SavingGoal> getGoalById(@PathVariable String goalId) {
        SavingGoal goal = goalService.getGoalById(goalId);
        if (goal == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(goal);
    }
}