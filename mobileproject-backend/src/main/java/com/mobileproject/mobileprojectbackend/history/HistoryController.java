package com.mobileproject.mobileprojectbackend.history;

import com.mobileproject.mobileprojectbackend.auth.AuthIdentityService;
import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import com.mobileproject.mobileprojectbackend.history.dto.HistoryListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller cho lịch sử xem địa điểm.
 * Base path: {@code /api/history}
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>POST /view?placeId=... – ghi nhận lượt xem</li>
 *   <li>GET  / – lấy lịch sử xem</li>
 *   <li>DELETE / – xóa toàn bộ lịch sử</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/history")
@RequiredArgsConstructor
public class HistoryController {

    private final HistoryService historyService;
    private final AuthIdentityService authIdentityService;

    /** Ghi nhận lượt xem địa điểm. */
    @PostMapping("/view")
    public ResponseEntity<Map<String, Object>> recordView(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @RequestParam String placeId) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        return ResponseEntity.ok(historyService.recordView(user.getId(), placeId));
    }

    /** Lấy lịch sử xem địa điểm (mới nhất trước). */
    @GetMapping
    public ResponseEntity<HistoryListResponse> getHistory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        return ResponseEntity.ok(historyService.getUserHistory(user.getId()));
    }

    /** Xóa toàn bộ lịch sử xem. */
    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearHistory(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        AuthUser user = authIdentityService.requireCurrentUser(authHeader);
        return ResponseEntity.ok(historyService.clearHistory(user.getId()));
    }
}
