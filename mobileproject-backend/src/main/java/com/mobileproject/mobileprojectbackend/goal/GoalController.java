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

/**
 * REST Controller quản lý mục tiêu (Goals) của cặp đôi.
 *
 * <p>Base path: {@code /api/v1/goals}</p>
 *
 * <p>Hỗ trợ cả hai loại mục tiêu:</p>
 * <ul>
 *   <li><b>SAVING</b> – Mục tiêu tiết kiệm (đóng góp tiền, theo dõi số tiền)</li>
 *   <li><b>FUTURE</b> – Mục tiêu tương lai (checklist công việc, theo dõi tiến độ %)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/goals")
public class GoalController {

    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    /**
     * Tạo mục tiêu mới cho cặp đôi.
     *
     * <p><b>POST</b> {@code /api/v1/goals}</p>
     *
     * @param request {@link CreateGoalRequest} chứa thông tin mục tiêu
     * @return {@link GoalResponse} với thông tin mục tiêu đã tạo; 400 nếu lỗi
     */
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

    /**
     * Đóng góp tiền vào mục tiêu tiết kiệm từ ví chung.
     *
     * <p><b>POST</b> {@code /api/v1/goals/{goalId}/contribute-from-wallet}</p>
     * <p>Trừ tiền từ ví chung và cộng vào mục tiêu.</p>
     *
     * @param goalId  ID mục tiêu (path variable)
     * @param request {@link ContributeFromWalletRequest} chứa amount, note
     * @return {@link ContributeResponse} với kết quả; 400 nếu số dư không đủ hoặc lỗi
     */
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

    /**
     * Đóng góp tiền trực tiếp vào mục tiêu (không trừ ví).
     *
     * <p><b>POST</b> {@code /api/v1/goals/{goalId}/contribute}</p>
     * <p>Dùng khi nạp tiền trực tiếp qua hệ thống thanh toán.</p>
     *
     * @param goalId  ID mục tiêu (path variable)
     * @param request {@link ContributeRequest} chứa amount, contributorId, note
     * @return {@link ContributeResponse} với kết quả; 400 nếu lỗi
     */
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

    /**
     * Lấy tất cả mục tiêu của một cặp đôi.
     *
     * <p><b>GET</b> {@code /api/v1/goals/couple/{coupleId}}</p>
     *
     * @param coupleId ID cặp đôi (path variable)
     * @return danh sách tất cả mục tiêu (SAVING + FUTURE)
     */
    @GetMapping("/couple/{coupleId}")
    public ResponseEntity<List<Goal>> getGoalsByCouple(@PathVariable String coupleId) {
        List<Goal> goals = goalService.getGoalsByCouple(coupleId);
        return ResponseEntity.ok(goals);
    }

    /**
     * Lấy chi tiết mục tiêu theo ID.
     *
     * <p><b>GET</b> {@code /api/v1/goals/{goalId}}</p>
     *
     * @param goalId ID mục tiêu (path variable)
     * @return {@link Goal} (SavingGoal hoặc FutureGoal); 404 nếu không tìm thấy
     */
    @GetMapping("/{goalId}")
    public ResponseEntity<Goal> getGoalById(@PathVariable String goalId) {
        Goal goal = goalService.getGoalById(goalId);
        if (goal == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(goal);
    }

    /**
     * Toggle trạng thái hoàn thành của task trong mục tiêu tương lai.
     *
     * <p><b>PATCH</b> {@code /api/v1/goals/{goalId}/tasks/{taskId}}</p>
     *
     * @param goalId ID mục tiêu (path variable)
     * @param taskId ID công việc (path variable)
     * @return {@link ToggleTaskResponse} với tiến độ cập nhật; 400 nếu lỗi
     */
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
