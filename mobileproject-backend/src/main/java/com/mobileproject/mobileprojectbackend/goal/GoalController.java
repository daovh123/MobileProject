package com.mobileproject.mobileprojectbackend.goal;

import com.mobileproject.mobileprojectbackend.goal.dto.ContributeFromWalletRequest;
import com.mobileproject.mobileprojectbackend.goal.dto.ContributeRequest;
import com.mobileproject.mobileprojectbackend.goal.dto.ContributeResponse;
import com.mobileproject.mobileprojectbackend.goal.dto.CreateGoalRequest;
import com.mobileproject.mobileprojectbackend.goal.dto.GoalResponse;
import com.mobileproject.mobileprojectbackend.goal.dto.ToggleTaskResponse;
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
                request.category(),
                request.type(),
                request.targetAmount(),
                request.deadline(),
                request.tasks()
        );
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @PostMapping("/{goalId}/contribute-from-wallet")
    public ResponseEntity<ContributeResponse> contributeFromWallet(
            @PathVariable String goalId,
            @RequestBody ContributeFromWalletRequest request) {
        ContributeResponse response = goalService.contributeFromWallet(
                goalId,
                request.amount(),
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

    @PostMapping("/{goalId}/withdraw-to-wallet")
    public ResponseEntity<ContributeResponse> withdrawGoalToWallet(@PathVariable String goalId) {
        ContributeResponse response = goalService.withdrawGoalToWallet(goalId);
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    @GetMapping("/couple/{coupleId}")
    public ResponseEntity<List<Goal>> getGoalsByCouple(@PathVariable String coupleId) {
        List<Goal> goals = goalService.getGoalsByCouple(coupleId);
        return ResponseEntity.ok(goals);
    }

    @GetMapping("/{goalId}")
    public ResponseEntity<Goal> getGoalById(@PathVariable String goalId) {
        Goal goal = goalService.getGoalById(goalId);
        if (goal == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(goal);
    }

    @PatchMapping("/{goalId}/tasks/{taskId}")
    public ResponseEntity<ToggleTaskResponse> toggleTask(
            @PathVariable String goalId,
            @PathVariable String taskId) {
        ToggleTaskResponse response = goalService.toggleTask(goalId, taskId);
        if (response.success()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }
}
