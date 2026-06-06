package com.mobileproject.mobileprojectbackend.moment;

import com.mobileproject.mobileprojectbackend.auth.AuthIdentityService;
import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.moment.comment.MomentCommentService;
import com.mobileproject.mobileprojectbackend.moment.comment.dto.MomentCommentRequest;
import com.mobileproject.mobileprojectbackend.moment.comment.dto.MomentCommentView;
import com.mobileproject.mobileprojectbackend.moment.dto.MomentRequest;
import com.mobileproject.mobileprojectbackend.moment.dto.MomentResponse;
import com.mobileproject.mobileprojectbackend.moment.dto.MomentView;
import com.mobileproject.mobileprojectbackend.moment.reaction.MomentReactionService;
import com.mobileproject.mobileprojectbackend.moment.reaction.dto.MomentReactionRequest;
import com.mobileproject.mobileprojectbackend.moment.reaction.dto.MomentReactionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller quản lý kỷ niệm (moment) của cặp đôi.
 * Base path: {@code /api/v1/moments}
 *
 * <p>Các endpoint yêu cầu xác thực qua header Authorization (Bearer token).
 * Tự động resolve coupleId từ thông tin user đã đăng nhập.</p>
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>GET  / – Lấy danh sách kỷ niệm của couple</li>
 *   <li>POST / – Tạo kỷ niệm mới (base64 image)</li>
 *   <li>POST /{momentId}/reactions – Thả reaction cho kỷ niệm</li>
 *   <li>GET  /{momentId}/comments – Lấy danh sách bình luận</li>
 *   <li>POST /{momentId}/comments – Thêm bình luận</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/v1/moments")
public class MomentController {

    private final MomentService momentService;
    private final AuthIdentityService authIdentityService;
    private final CoupleInfoRepository coupleInfoRepository;
    private final MomentReactionService momentReactionService;
    private final MomentCommentService momentCommentService;

    public MomentController(
            MomentService momentService,
            AuthIdentityService authIdentityService,
            CoupleInfoRepository coupleInfoRepository,
            MomentReactionService momentReactionService,
            MomentCommentService momentCommentService) {
        this.momentService = momentService;
        this.authIdentityService = authIdentityService;
        this.coupleInfoRepository = coupleInfoRepository;
        this.momentReactionService = momentReactionService;
        this.momentCommentService = momentCommentService;
    }

    /**
     * Lấy danh sách kỷ niệm của couple hiện tại.
     */
    @GetMapping
    public ResponseEntity<List<MomentView>> getMoments(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestParam String coupleId) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        String resolvedCoupleId = resolveCoupleId(user);
        if (resolvedCoupleId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(momentService.getMoments(resolvedCoupleId, user.getId()));
    }

    /**
     * Tạo kỷ niệm mới với ảnh base64.
     */
    @PostMapping
    public ResponseEntity<MomentResponse> createMoment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @Valid @RequestBody MomentRequest request) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        String resolvedCoupleId = resolveCoupleId(user);
        if (resolvedCoupleId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MomentResponse(false, "User is not paired", null));
        }

        try {
            MomentView moment = momentService.saveMoment(
                    resolvedCoupleId,
                    request.title(),
                    request.base64Image(),
                    user.getId());
            return ResponseEntity.ok(new MomentResponse(true, "Moment created successfully", moment));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new MomentResponse(false, ex.getMessage(), null));
        }
    }

    /**
     * Thả reaction cho kỷ niệm. Nếu đã reaction cùng loại → hủy, khác loại → đổi.
     */
    @PostMapping("/{momentId}/reactions")
    public ResponseEntity<MomentReactionResponse> reactToMoment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String momentId,
            @Valid @RequestBody MomentReactionRequest request) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        String resolvedCoupleId = resolveCoupleId(user);
        if (resolvedCoupleId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        momentService.requireMomentInCouple(momentId, resolvedCoupleId);
        MomentReactionResponse response = momentReactionService.react(
                momentId,
                resolvedCoupleId,
                user.getId(),
                request.reaction());
        return ResponseEntity.ok(response);
    }

    /**
     * Lấy danh sách bình luận của kỷ niệm.
     */
    @GetMapping("/{momentId}/comments")
    public ResponseEntity<List<MomentCommentView>> getComments(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String momentId) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        String resolvedCoupleId = resolveCoupleId(user);
        if (resolvedCoupleId == null) {
            return ResponseEntity.ok(List.of());
        }

        momentService.requireMomentInCouple(momentId, resolvedCoupleId);
        return ResponseEntity.ok(momentCommentService.getComments(momentId));
    }

    /**
     * Thêm bình luận cho kỷ niệm.
     */
    @PostMapping("/{momentId}/comments")
    public ResponseEntity<MomentCommentView> addComment(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @PathVariable String momentId,
            @Valid @RequestBody MomentCommentRequest request) {
        AuthUser user = authIdentityService.requireCurrentUser(authorizationHeader);
        String resolvedCoupleId = resolveCoupleId(user);
        if (resolvedCoupleId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        momentService.requireMomentInCouple(momentId, resolvedCoupleId);
        MomentCommentView comment = momentCommentService.addComment(
                momentId,
                resolvedCoupleId,
                user,
                request.content());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    private String resolveCoupleId(AuthUser user) {
        if (user == null || user.getId() == null) {
            return null;
        }
        CoupleInfo coupleInfo = coupleInfoRepository.findFirstByIdUser1OrIdUser2(user.getId(), user.getId())
                .orElse(null);
        if (coupleInfo == null) {
            return null;
        }
        return coupleInfo.getIdCouple();
    }
}
