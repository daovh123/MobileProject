package com.mobileproject.mobileprojectbackend.moment.comment;

import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import com.mobileproject.mobileprojectbackend.moment.comment.dto.MomentCommentView;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service xử lý bình luận cho kỷ niệm.
 *
 * <p><strong>Business logic:</strong></p>
 * <ul>
 *   <li>Lấy danh sách bình luận theo thứ tự thời gian tăng dần</li>
 *   <li>Thêm bình luận mới với tên hiển thị của người gửi</li>
 * </ul>
 */
@Service
public class MomentCommentService {

    private final MomentCommentRepository momentCommentRepository;

    public MomentCommentService(MomentCommentRepository momentCommentRepository) {
        this.momentCommentRepository = momentCommentRepository;
    }

    /**
     * Lấy danh sách bình luận của moment (cũ nhất trước).
     */
    public List<MomentCommentView> getComments(String momentId) {
        return momentCommentRepository.findByMomentIdOrderByCreatedAtAsc(momentId).stream()
                .map(this::toView)
                .toList();
    }

    /**
     * Thêm bình luận mới cho kỷ niệm.
     *
     * @param momentId ID kỷ niệm
     * @param coupleId ID couple
     * @param user     người bình luận
     * @param content  nội dung (tối đa 300 ký tự)
     * @return bình luận đã lưu
     */
    public MomentCommentView addComment(String momentId, String coupleId, AuthUser user, String content) {
        String author = user == null ? "" : user.getUsername();
        String userId = user == null ? "" : user.getId();
        String safeContent = content == null ? "" : content.trim();
        MomentComment comment = new MomentComment(momentId, coupleId, userId, author, safeContent);
        return toView(momentCommentRepository.save(comment));
    }

    private MomentCommentView toView(MomentComment comment) {
        return new MomentCommentView(
                comment.getId(),
                comment.getMomentId(),
                comment.getAuthorUsername(),
                comment.getContent(),
                comment.getCreatedAt());
    }
}
