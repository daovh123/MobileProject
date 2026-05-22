package com.mobileproject.mobileprojectbackend.moment;

import com.mobileproject.mobileprojectbackend.auth.CoupleInfo;
import com.mobileproject.mobileprojectbackend.auth.CoupleInfoRepository;
import com.mobileproject.mobileprojectbackend.moment.comment.MomentCommentRepository;
import com.mobileproject.mobileprojectbackend.moment.dto.MomentReactionSummary;
import com.mobileproject.mobileprojectbackend.moment.dto.MomentView;
import com.mobileproject.mobileprojectbackend.moment.reaction.MomentReaction;
import com.mobileproject.mobileprojectbackend.moment.reaction.MomentReactionRepository;
import com.mobileproject.mobileprojectbackend.notifications.NotificationService;
import com.mobileproject.mobileprojectbackend.notifications.NotificationType;
import com.mobileproject.mobileprojectbackend.storage.FirebaseStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MomentService {
    private final MomentRepository momentRepository;
    private final MomentReactionRepository momentReactionRepository;
    private final MomentCommentRepository momentCommentRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final NotificationService notificationService;
    private final CoupleInfoRepository coupleInfoRepository;

    public MomentService(
            MomentRepository momentRepository,
            MomentReactionRepository momentReactionRepository,
            MomentCommentRepository momentCommentRepository,
            FirebaseStorageService firebaseStorageService,
            NotificationService notificationService,
            CoupleInfoRepository coupleInfoRepository) {
        this.momentRepository = momentRepository;
        this.momentReactionRepository = momentReactionRepository;
        this.momentCommentRepository = momentCommentRepository;
        this.firebaseStorageService = firebaseStorageService;
        this.notificationService = notificationService;
        this.coupleInfoRepository = coupleInfoRepository;
    }

    public MomentView saveMoment(String coupleId, String title, String base64Image, String viewerUserId) {
        if (coupleId == null || coupleId.isBlank()) {
            throw new IllegalArgumentException("Couple id is required");
        }
        if (base64Image == null || base64Image.isBlank()) {
            throw new IllegalArgumentException("Image data is required");
        }

        String cleanBase64 = base64Image.trim();
        String contentType = "image/jpeg";
        if (cleanBase64.contains(",")) {
            String[] parts = cleanBase64.split(",", 2);
            String header = parts[0];
            cleanBase64 = parts[1];
            if (header.contains("image/png"))
                contentType = "image/png";
            else if (header.contains("image/webp"))
                contentType = "image/webp";
            else if (header.contains("image/gif"))
                contentType = "image/gif";
        }

        byte[] imageBytes;
        try {
            imageBytes = Base64.getDecoder().decode(cleanBase64);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid image data");
        }

        String imageUrl = firebaseStorageService.uploadMomentImage(coupleId, imageBytes, contentType);
        String safeTitle = title == null ? "" : title.trim();
        Moment moment = new Moment(coupleId, safeTitle, imageUrl);
        MomentView savedView = toView(momentRepository.save(moment), viewerUserId);

        // Notify partner about new memory
        if (viewerUserId != null) {
            CoupleInfo couple = coupleInfoRepository.findById(coupleId).orElse(null);
            if (couple != null) {
                notificationService.createAndPushForPartner(coupleId, viewerUserId,
                        NotificationType.PARTNER_MEMORY,
                        "Kỷ niệm mới! 📸",
                        "Đối phương vừa chia sẻ một kỷ niệm mới" + (safeTitle.isBlank() ? "." : ": " + safeTitle));
            }
        }

        return savedView;
    }

    public List<MomentView> getMoments(String coupleId, String viewerUserId) {
        return momentRepository.findByCoupleIdOrderByCreatedAtDesc(coupleId).stream()
                .map(moment -> toView(moment, viewerUserId))
                .toList();
    }

    public Moment requireMomentInCouple(String momentId, String coupleId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Moment not found"));
        if (coupleId == null || !coupleId.equals(moment.getCoupleId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Couple access denied");
        }
        return moment;
    }

    private MomentView toView(Moment moment, String viewerUserId) {
        List<MomentReaction> reactions = momentReactionRepository.findByMomentIdOrderByCreatedAtDesc(moment.getId());
        Map<String, Long> counts = reactions.stream()
                .collect(Collectors.groupingBy(MomentReaction::getReaction, Collectors.counting()));
        List<MomentReactionSummary> summary = counts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new MomentReactionSummary(entry.getKey(), entry.getValue().intValue()))
                .toList();

        String viewerReaction = null;
        if (viewerUserId != null) {
            viewerReaction = reactions.stream()
                    .filter(reaction -> viewerUserId.equals(reaction.getUserId()))
                    .map(MomentReaction::getReaction)
                    .findFirst()
                    .orElse(null);
        }

        int commentsCount = (int) momentCommentRepository.countByMomentId(moment.getId());

        return new MomentView(
                moment.getId(),
                moment.getCoupleId(),
                moment.getTitle(),
                moment.getImageUrl(),
                moment.getCreatedAt(),
                reactions.size(),
                commentsCount,
                viewerReaction,
                summary);
    }

}
