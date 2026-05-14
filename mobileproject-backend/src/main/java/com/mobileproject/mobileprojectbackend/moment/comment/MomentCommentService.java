package com.mobileproject.mobileprojectbackend.moment.comment;

import com.mobileproject.mobileprojectbackend.auth.AuthUser;
import com.mobileproject.mobileprojectbackend.moment.comment.dto.MomentCommentView;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MomentCommentService {

    private final MomentCommentRepository momentCommentRepository;

    public MomentCommentService(MomentCommentRepository momentCommentRepository) {
        this.momentCommentRepository = momentCommentRepository;
    }

    public List<MomentCommentView> getComments(String momentId) {
        return momentCommentRepository.findByMomentIdOrderByCreatedAtAsc(momentId).stream()
                .map(this::toView)
                .toList();
    }

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
